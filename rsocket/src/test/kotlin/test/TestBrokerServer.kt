package test

import com.github.ixtf.broker.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.internal.ConnectionSetupPayloadBuilder.Companion.buildConnectionSetupPayload
import com.github.ixtf.broker.internal.tcpServerTransport
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.rsocket.Closeable
import io.rsocket.ConnectionSetupPayload
import io.rsocket.RSocket
import io.rsocket.SocketAcceptor
import io.rsocket.core.RSocketServer
import io.rsocket.frame.decoder.PayloadDecoder
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.coAwait
import kotlinx.coroutines.reactor.awaitSingle
import reactor.core.publisher.Mono

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
        .bind(tcpServerTransport(IXTF_API_BROKER_TARGET))
        .awaitSingle()
  }

  override fun accept(setup: ConnectionSetupPayload, sendingSocket: RSocket): Mono<RSocket> {
    buildConnectionSetupPayload(service = "service") {}
    TODO("Not yet implemented")
  }
}
