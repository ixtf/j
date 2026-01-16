package com.github.ixtf.broker.verticle

import com.github.ixtf.broker.Env.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.internal.ClientTarget
import com.github.ixtf.broker.internal.InternalKit
import com.github.ixtf.broker.internal.InternalKit.buildConnectionSetupPayload
import com.github.ixtf.broker.internal.InternalKit.doAfterTerminate
import com.github.ixtf.broker.internal.domain.RSocketStatus
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.rsocket.ConnectionSetupPayload
import io.rsocket.RSocket
import io.rsocket.SocketAcceptor
import io.rsocket.core.RSocketClient
import io.rsocket.core.RSocketConnector
import io.rsocket.frame.decoder.PayloadDecoder
import kotlin.properties.Delegates
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

abstract class BrokerServiceVerticle(token: String, target: String = IXTF_API_BROKER_TARGET) :
  BaseCoroutineVerticle(), SocketAcceptor, RSocket {
  private val rSocketClientSourceMono =
    RSocketConnector.create()
      .acceptor(this)
      .payloadDecoder(PayloadDecoder.ZERO_COPY)
      .setupPayload(buildConnectionSetupPayload(token))
      .reconnect(InternalKit.defaultRetry(this@BrokerServiceVerticle))
      .connect(ClientTarget(target).transport())
  private var status: RSocketStatus by
    Delegates.observable(RSocketStatus.INIT) { _, old, new ->
      log.warn("${this}: $old -> $new")
    }
  private lateinit var rSocketClient: RSocketClient

  private fun reConnect() {
    if (::rSocketClient.isInitialized) {
      status = RSocketStatus.DOWN
      rSocketClient.dispose()
    }
    rSocketClient = RSocketClient.from(rSocketClientSourceMono)
    rSocketClient.connect()
  }

  override fun accept(setup: ConnectionSetupPayload, sendingSocket: RSocket): Mono<RSocket> = mono {
    sendingSocket.doAfterTerminate { vertx.runOnContext { reConnect() } }
    status = RSocketStatus.UP
    this@BrokerServiceVerticle
  }

  override suspend fun start() {
    super.start()
    reConnect()
  }

  override suspend fun stop() {
    rSocketClient.dispose()
    super.stop()
  }
}
