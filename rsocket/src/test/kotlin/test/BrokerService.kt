package test

import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.rsocket.ConnectionSetupPayload
import io.rsocket.RSocket
import io.rsocket.SocketAcceptor
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.coAwait
import reactor.core.publisher.Mono

private val vertx = Vertx.vertx()

suspend fun main() {
  vertx.deployVerticle(BrokerService()).coAwait()
}

private class BrokerService : BaseCoroutineVerticle(), SocketAcceptor {
  override fun accept(setup: ConnectionSetupPayload, sendingSocket: RSocket): Mono<RSocket> {
    TODO("Not yet implemented")
  }

  override suspend fun start() {
    super.start()
  }
}
