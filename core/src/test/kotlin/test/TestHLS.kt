package test

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.HexUtil
import cn.hutool.log.Log
import com.github.ixtf.core.kit.filename
import io.lindstrom.m3u8.model.MediaSegment
import io.lindstrom.m3u8.parser.MediaPlaylistParser
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpResponseExpectation.SC_SUCCESS
import io.vertx.core.impl.cpu.CpuCoreSensor
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.coAwait
import java.net.URI
import java.nio.file.Path
import java.security.Key
import java.security.spec.AlgorithmParameterSpec
import java.time.Duration
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import reactor.util.retry.Retry

private val vertx = Vertx.vertx()
private val webClient = WebClient.create(vertx)
private val fs = vertx.fileSystem()

suspend fun main() {
  val filename = "周宁"
  val verticle =
    HLSVerticle(
      m3u8S =
        "TXBfNDRQY2Nwb2ZlR1hPLUk1M1UzbXB1YmxBNVEwVm1SWHB1ZDJreWFFbE9TMWR0UlVGTGRuaE1kSGxFYkdNMldXTlJlVmxqU1cxSFNXRkJUMjV5VDBaVmJubDZZVmxVYWpST2EzTmpTMFp4YjBNNVVXOTBTWE5xWTBOVlpteDJOM0ZyZEhCelREQkJjVlk0YTAxc1dTdHhZMmwzTW1oWGVURnZQUT09",
      //          baseUri = URI("https://s1.playhls.com"),
      baseUri = URI("https://s2.playhls.com"),
      dlDirPath = Path("/tmp/$filename"),
      outPath = Path("/Users/jzb/Movies/$filename.mp4"),
    )
  vertx.deployVerticle(verticle).coAwait()

  println("success")
}

private class HLSVerticle(
  private val baseUri: URI,
  private val m3u8S: String,
  private val dlDirPath: Path,
  private val outPath: Path,
) : CoroutineVerticle() {
  private val log = Log.get(this.javaClass)
  private var dlError = false
  private var mergeError = false
  private lateinit var m3u8AesFuture: Future<Pair<Key, AlgorithmParameterSpec>>
  private val retry =
    Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
      .maxBackoff(Duration.ofSeconds(3))
      .jitter(0.5)
      .onRetryExhaustedThrow { spec, signal -> RuntimeException("重连失败，放弃连接") }
      .doBeforeRetry { signal -> log.error("连接断开，尝试第 ${signal.totalRetries() + 1} 次重连...") }

  override suspend fun start() {
    super.start()
    log.info("dlDirPath: $dlDirPath")
    log.info("outPath: $outPath")
    launch {
      val mediaSegmentChannel = mediaSegmentChannel()
      List(CpuCoreSensor.availableProcessors()) { mediaSegmentChannel.dl() }.joinAll()
      merge()
      vertx.undeploy(deploymentID)
    }
  }

  override suspend fun stop() {
    super.stop()
    println("dlError: $dlError; mergeError: $mergeError")
  }

  private fun ReceiveChannel<MediaSegment>.dl() = launch {
    consumeEach { mediaSegment ->
      mono {
          webClient.ensureM3u8Aes(mediaSegment)
          webClient.dl(mediaSegment)
        }
        .retryWhen(retry)
        .awaitSingleOrNull()
      //      runCatching {
      //            webClient.ensureM3u8Aes(mediaSegment)
      //            webClient.dl(mediaSegment)
      //          }
      //          .onFailure {
      //            dlError = true
      //            log.error(it, "uri[${mediaSegment.uri()}]")
      //          }
    }
  }

  private suspend fun merge() {
    require(!dlError)
    outPath.toFile().outputStream().use { outputStream ->
      FileUtil.loopFiles(dlDirPath.toFile()).sorted().forEach { file ->
        val (key, ivSpec) = m3u8AesFuture.coAwait()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
        val buffer = fs.readFile("$file").coAwait()
        val decrypt = cipher.update(buffer.bytes)
        outputStream.write(decrypt)
      }
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  private fun mediaSegmentChannel() =
    produce(capacity = Channel.UNLIMITED) {
      val m3u8 = webClient.m3u8(m3u8S)
      val playlist = MediaPlaylistParser().readPlaylist("$m3u8")
      playlist.mediaSegments().forEach { send(it) }
    }

  private suspend fun WebClient.m3u8(s: String) =
    getAbs("${baseUri.resolve("/m3u8.php")}")
      .addQueryParam("s", s)
      .send()
      .expecting(SC_SUCCESS)
      .coAwait()
      .body()

  private suspend fun WebClient.dl(mediaSegment: MediaSegment) {
    val uri = mediaSegment.uri()
    val dlPath = dlDirPath.resolve(uri.filename())
    if (dlPath.exists()) return
    val body = getAbs(uri).send().expecting(SC_SUCCESS).coAwait().body()
    FileUtil.writeBytes(body.bytes, dlPath.toFile())
    log.info("$dlPath")
  }

  private fun WebClient.ensureM3u8Aes(mediaSegment: MediaSegment) {
    if (!::m3u8AesFuture.isInitialized) {
      val segmentKey = mediaSegment.segmentKeys().first()
      m3u8AesFuture =
        getAbs("${baseUri.resolve(segmentKey.uri().get())}")
          .send()
          .expecting(SC_SUCCESS)
          .map { SecretKeySpec(it.body().bytes, "AES") }
          .map { key ->
            val iv = HexUtil.decodeHex(segmentKey.iv().get().substring(2))
            val ivSpec = IvParameterSpec(iv)
            key to ivSpec
          }
    }
  }
}
