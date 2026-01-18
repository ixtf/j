package test

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.HexUtil
import cn.hutool.log.Log
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.ixtf.core.MAPPER
import com.github.ixtf.core.kit.filename
import io.lindstrom.m3u8.model.MediaSegment
import io.lindstrom.m3u8.parser.MediaPlaylistParser
import io.vertx.core.Future
import io.vertx.core.ThreadingModel.VIRTUAL_THREAD
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpResponseExpectation.SC_SUCCESS
import io.vertx.core.impl.cpu.CpuCoreSensor
import io.vertx.ext.reactivestreams.ReactiveReadStream
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.core.file.openOptionsOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.coAwait
import java.net.URI
import java.time.Duration
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.text.substring
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.flux
import kotlinx.coroutines.reactor.mono
import reactor.util.retry.Retry

private val vertx = Vertx.vertx()
private val webClient = WebClient.create(vertx)
private val fs = vertx.fileSystem()
private val baseUri = URI("https://video.xchina.download")

suspend fun main() {
  val downloadState =
    DownloadState.loadState(
      "63c9e2636de4c",
      "https://video.xchina.download/m3u8/63c9e2636de4c/720.m3u8?expires=1766593202&md5=mTLv8mEGR3yYUdjeG9CaAA",
    )

  val verticle = HLS2Verticle(downloadState = downloadState)
  vertx.deployVerticle(verticle, deploymentOptionsOf(threadingModel = VIRTUAL_THREAD)).coAwait()

  println("success")
}

private data class DownloadState(val id: String, val m3u8: String) {
  companion object {
    suspend fun loadState(id: String, m3u8: String): DownloadState {
      val path = "/tmp/$id.download.state"
      if (fs.exists(path).coAwait()) {
        val buffer = fs.readFile(path).coAwait()
        return MAPPER.readValue<DownloadState>(buffer.bytes)
      }
      val m3u8 = webClient.getAbs(m3u8).send().expecting(SC_SUCCESS).coAwait().bodyAsString()
      val downloadState = DownloadState(id = id, m3u8 = m3u8)
      val bytes = MAPPER.writeValueAsBytes(downloadState)
      fs.writeFile(path, Buffer.buffer(bytes)).coAwait()
      return downloadState
    }
  }
}

private class HLS2Verticle(private val downloadState: DownloadState) : CoroutineVerticle() {
  private val log = Log.get(this.javaClass)
  private val retry =
    Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
      .maxBackoff(Duration.ofSeconds(3))
      .jitter(0.5)
      .onRetryExhaustedThrow { spec, signal -> RuntimeException("重连失败，放弃连接") }
      .doBeforeRetry { signal -> log.error("连接断开，尝试第 ${signal.totalRetries() + 1} 次重连...") }

  private val outPath = Path("${FileUtil.getUserHomePath()}/Movies/${downloadState.id}.mp4")
  private val dlDirPath = Path("/tmp/${downloadState.id}").also { FileUtil.mkdir(it) }

  private lateinit var keyFuture: Future<HttpResponse<Buffer>>
  private lateinit var iv: String
  private val m3u8AesFuture by lazy {
    keyFuture.map { keyBuffer ->
      val key = SecretKeySpec(keyBuffer.body().bytes, "AES")
      val iv = HexUtil.decodeHex(iv.substring(2))
      key to IvParameterSpec(iv)
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  private val mediaSegmentChannel by lazy {
    produce(capacity = Channel.UNLIMITED) {
      MediaPlaylistParser().readPlaylist(downloadState.m3u8).mediaSegments().forEach { mediaSegment
        ->
        mediaSegment.segmentKeys().forEach { segmentKey ->
          segmentKey.iv().ifPresent { iv = it }
          segmentKey.uri().ifPresent { keyUri ->
            keyFuture = webClient.getAbs("${baseUri.resolve(keyUri)}").send().expecting(SC_SUCCESS)
          }
        }
        send(mediaSegment)
      }
    }
  }

  override suspend fun start() {
    super.start()
    log.info("dlDirPath: $dlDirPath")
    launch {
      List(CpuCoreSensor.availableProcessors()) { mediaSegmentChannel.dl() }.joinAll()
      merge()
      vertx.undeploy(deploymentID)
    }
  }

  override suspend fun stop() {
    log.info("outPath: $outPath")
    super.stop()
  }

  private fun ReceiveChannel<MediaSegment>.dl() = launch {
    consumeEach { mediaSegment ->
      mono { webClient.dl(mediaSegment) }.retryWhen(retry).awaitSingleOrNull()
    }
  }

  private suspend fun merge() {
    val rrs = ReactiveReadStream.readStream<Buffer>()
    flux {
        val (key, ivSpec) = m3u8AesFuture.coAwait()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        FileUtil.loopFiles(dlDirPath.toFile()).sorted().forEach { file ->
          cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
          val buffer = fs.readFile("$file").coAwait()
          val decrypt = cipher.update(buffer.bytes)
          send(Buffer.buffer(decrypt))
        }
      }
      .subscribe(rrs)
    val asyncFile = fs.open("$outPath", openOptionsOf()).coAwait()
    rrs.pipeTo(asyncFile).coAwait()
  }

  private suspend fun WebClient.dl(mediaSegment: MediaSegment) {
    val uri = baseUri.resolve(mediaSegment.uri())
    val dlPath = dlDirPath.resolve(uri.path.filename())
    if (dlPath.exists()) return
    val body =
      getAbs("$uri")
        .putHeader("Referer", "https://xchina.co/video/id-${downloadState.id}.html")
        .send()
        .expecting(SC_SUCCESS)
        .coAwait()
        .body()
    log.info("$dlPath")
    fs.writeFile("$dlPath", body).coAwait()
  }
}
