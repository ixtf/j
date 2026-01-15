package com.github.ixtf.broker.verticle

import com.github.ixtf.broker.Env.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.dto.SetupDTO
import com.github.ixtf.broker.internal.InternalKit
import com.github.ixtf.broker.internal.InternalKit.buildConnectionSetupPayload
import com.github.ixtf.broker.internal.InternalKit.defaultAuth
import com.github.ixtf.broker.internal.InternalKit.doAfterTerminate
import com.github.ixtf.broker.internal.InternalKit.tcpClientTransport
import com.github.ixtf.core.J
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.rsocket.ConnectionSetupPayload
import io.rsocket.RSocket
import io.rsocket.SocketAcceptor
import io.rsocket.core.RSocketClient
import io.rsocket.core.RSocketConnector
import io.rsocket.frame.decoder.PayloadDecoder
import io.rsocket.util.DefaultPayload
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.ext.auth.jwtOptionsOf
import kotlin.properties.Delegates
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

enum class BrokerServiceStatus {
  INIT,
  UP,
  DOWN,
  DISPOSE,
}

abstract class BrokerServiceVerticle(
  service: String,
  instance: String,
  tags: Set<String>? = null,
  host: String = J.localIp(),
  target: String = IXTF_API_BROKER_TARGET,
) : BaseCoroutineVerticle(), SocketAcceptor, RSocket {
  protected open val jwtAuth by lazy { vertx.defaultAuth() }
  private val rSocketClient =
    RSocketClient.from(
      RSocketConnector.create()
        .acceptor(this)
        .payloadDecoder(PayloadDecoder.ZERO_COPY)
        .setupPayload(
          buildConnectionSetupPayload {
            SetupDTO(
              host = host,
              service = service,
              instance = instance,
              tags = tags,
              token =
                jwtAuth?.generateToken(
                  jsonObjectOf("sub" to instance),
                  jwtOptionsOf(noTimestamp = true),
                ),
            )
          }
        )
        .reconnect(InternalKit.defaultRetry(this@BrokerServiceVerticle))
        .connect(tcpClientTransport(target))
    )
  protected var status: BrokerServiceStatus by
    Delegates.observable(BrokerServiceStatus.INIT) { _, old, new ->
      if (old == new) return@observable
      log.warn("${this@BrokerServiceVerticle}: $old -> $new")
      if (rSocketClient.isDisposed) {
        status = BrokerServiceStatus.DISPOSE
        return@observable
      }
      when (new) {
        BrokerServiceStatus.INIT,
        BrokerServiceStatus.UP,
        BrokerServiceStatus.DISPOSE -> Unit
        BrokerServiceStatus.DOWN -> reConnect()
      }
    }
    private set

  override fun accept(setup: ConnectionSetupPayload, sendingSocket: RSocket): Mono<RSocket> = mono {
    vertx.runOnContext { status = BrokerServiceStatus.UP }
    sendingSocket.doAfterTerminate { vertx.runOnContext { status = BrokerServiceStatus.DOWN } }
    this@BrokerServiceVerticle
  }

  private fun reConnect() {
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
