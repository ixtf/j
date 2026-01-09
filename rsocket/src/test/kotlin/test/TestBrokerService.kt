package test

import com.github.ixtf.broker.internal.ConnectionSetupPayloadBuilder.Companion.buildConnectionSetupPayload
import com.github.ixtf.broker.internal.tcpClientTransport
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.rsocket.ConnectionSetupPayload
import io.rsocket.RSocket
import io.rsocket.SocketAcceptor
import io.rsocket.core.RSocketConnector
import io.rsocket.frame.decoder.PayloadDecoder
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.coAwait
import java.time.Duration
import reactor.core.publisher.Mono
import reactor.util.retry.Retry

private val vertx = Vertx.vertx()

suspend fun main() {
  vertx.deployVerticle(TestBrokerService()).coAwait()
}

private class TestBrokerService : BaseCoroutineVerticle(), SocketAcceptor {

  override suspend fun start() {
    super.start()
    RSocketConnector.create()
      .payloadDecoder(PayloadDecoder.ZERO_COPY)
      .setupPayload(buildSetup())
      .reconnect(buildRetry())
      .acceptor(this)
      .connect(buildClientTransport())
  }

  private fun buildSetup() = buildConnectionSetupPayload(service = "service") {}

  private fun buildClientTransport() = tcpClientTransport("localhost:12345")

  private fun buildRetry(): Retry =
    Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
      .maxBackoff(Duration.ofSeconds(3))
      .jitter(0.5)
      .onRetryExhaustedThrow { spec, signal -> RuntimeException("重连失败，放弃连接") }
      .doBeforeRetry { signal -> log.error("连接断开，尝试第 ${signal.totalRetries() + 1} 次重连...") }

  override fun accept(setup: ConnectionSetupPayload, sendingSocket: RSocket): Mono<RSocket> {
    TODO("Not yet implemented")
  }
}
