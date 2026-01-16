package com.github.ixtf.broker.verticle

import cn.hutool.core.util.RandomUtil
import com.github.ixtf.broker.Env.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.internal.ClientTarget
import com.github.ixtf.broker.internal.InternalKit
import com.github.ixtf.broker.internal.InternalKit.buildConnectionSetupPayload
import com.github.ixtf.broker.internal.InternalKit.doOnClose
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
  private val rSocketClient =
    RSocketClient.from(
      RSocketConnector.create()
        .acceptor(this)
        .payloadDecoder(PayloadDecoder.ZERO_COPY)
        .setupPayload(buildConnectionSetupPayload(token))
        .reconnect(InternalKit.defaultRetry(this))
        .connect(ClientTarget(target).transport())
    )
  private var status: RSocketStatus by
    Delegates.observable(RSocketStatus.INIT) { _, old, new ->
      log.warn("$old -> $new")
      if (status == RSocketStatus.DOWN) vertx.runOnContext { reConnect() }
    }

  private fun reConnect() {
    if (status == RSocketStatus.STOP) return
    vertx.setTimer(RandomUtil.randomLong(1000, 3000)) {
      if (status == RSocketStatus.DOWN) {
        if (rSocketClient.connect().not()) {
          log.error("reConnect failure")
          // TODO 重新初始化 rSocketClient
        }
      }
    }
  }

  override fun accept(setup: ConnectionSetupPayload, sendingSocket: RSocket): Mono<RSocket> = mono {
    sendingSocket.doOnClose {
      vertx.runOnContext { if (status != RSocketStatus.STOP) status = RSocketStatus.DOWN }
    }
    vertx.runOnContext { status = RSocketStatus.UP }
    this@BrokerServiceVerticle
  }

  override suspend fun start() {
    super.start()
    rSocketClient.connect()
  }

  override suspend fun stop() {
    status = RSocketStatus.STOP
    rSocketClient.dispose()
    super.stop()
  }
}
