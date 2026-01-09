package test

import com.github.ixtf.broker.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.internal.tcpServerTransport
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.rsocket.Closeable
import io.rsocket.ConnectionSetupPayload
import io.rsocket.RSocket
import io.rsocket.SocketAcceptor
import io.rsocket.core.RSocketServer
import io.rsocket.core.Resume
import io.rsocket.frame.decoder.PayloadDecoder
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.coAwait
import java.time.Duration
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono
import reactor.util.retry.Retry

private val vertx = Vertx.vertx()

suspend fun main() {
  vertx.deployVerticle(BrokerServer()).coAwait()
}

private class BrokerServer : BaseCoroutineVerticle(), SocketAcceptor {
  private lateinit var closeable: Closeable

  override suspend fun start() {
    super.start()
    closeable =
      RSocketServer.create(this)
        .payloadDecoder(PayloadDecoder.ZERO_COPY)
        .resume(
          Resume()
            .sessionDuration(Duration.ofMinutes(5)) // 允许服务端宕机 5 分钟内恢复 Session
            .retry(
              Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
                .maxBackoff(Duration.ofSeconds(3))
                .jitter(0.5)
                .doBeforeRetry { signal ->
                  log.error("${this@BrokerServer}，尝试第 ${signal.totalRetries() + 1} 次重连...")
                }
            )
        )
        .bind(tcpServerTransport(IXTF_API_BROKER_TARGET))
        .awaitSingle()

    log.info("BrokerServer started")
  }

  override suspend fun stop() {
    closeable.dispose()
    super.stop()
  }

  override fun accept(setup: ConnectionSetupPayload, sendingSocket: RSocket): Mono<RSocket> {
    println("test")
    return mono { object : RSocket {} }
  }
}
