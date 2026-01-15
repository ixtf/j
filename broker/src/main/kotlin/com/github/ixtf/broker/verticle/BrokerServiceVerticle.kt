package com.github.ixtf.broker.verticle

import com.github.ixtf.broker.Env.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.internal.InternalKit
import com.github.ixtf.broker.internal.InternalKit.buildConnectionSetupPayload
import com.github.ixtf.broker.internal.InternalKit.clientTransport
import com.github.ixtf.broker.internal.InternalKit.doAfterTerminate
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.rsocket.ConnectionSetupPayload
import io.rsocket.RSocket
import io.rsocket.SocketAcceptor
import io.rsocket.core.RSocketClient
import io.rsocket.core.RSocketConnector
import io.rsocket.frame.decoder.PayloadDecoder
import io.rsocket.util.DefaultPayload
import kotlin.properties.Delegates
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

enum class BrokerServiceStatus {
  INIT,
  UP,
  DOWN,
  DISPOSE,
}

abstract class BrokerServiceVerticle(token: String, target: String = IXTF_API_BROKER_TARGET) :
  BaseCoroutineVerticle(), SocketAcceptor, RSocket {
  private val rSocketClient =
    RSocketClient.from(
      RSocketConnector.create()
        .acceptor(this)
        .payloadDecoder(PayloadDecoder.ZERO_COPY)
        .setupPayload(buildConnectionSetupPayload(token))
        .reconnect(InternalKit.defaultRetry(this@BrokerServiceVerticle))
        .connect(clientTransport(target))
    )
  protected var status: BrokerServiceStatus by
    Delegates.observable(BrokerServiceStatus.INIT) { _, old, new ->
      if (old == new) return@observable
      log.warn("${this@BrokerServiceVerticle}: $old -> $new")
      if (BrokerServiceStatus.DOWN == new) reConnect()
    }
    private set

  override fun accept(setup: ConnectionSetupPayload, sendingSocket: RSocket): Mono<RSocket> = mono {
    vertx.runOnContext { status = BrokerServiceStatus.UP }
    sendingSocket.doAfterTerminate { vertx.runOnContext { status = BrokerServiceStatus.DOWN } }
    this@BrokerServiceVerticle
  }

  private fun reConnect() {
    if (rSocketClient.isDisposed || status == BrokerServiceStatus.DISPOSE) return
    rSocketClient
      .fireAndForget(mono { DefaultPayload.create(DefaultPayload.EMPTY_BUFFER) })
      .subscribe()
  }

  override suspend fun start() {
    super.start()
    rSocketClient.connect()
  }

  override suspend fun stop() {
    rSocketClient.dispose()
    super.stop()
  }
}
