package com.github.ixtf.broker.verticle

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.ixtf.broker.Env.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.internal.InternalKit
import com.github.ixtf.broker.internal.InternalKit.defaultAuth
import com.github.ixtf.broker.internal.ServerTarget
import com.github.ixtf.broker.internal.domain.RSocketServer
import com.github.ixtf.broker.internal.domain.RSocketServer.Companion.RSocketServerId
import com.github.ixtf.broker.readValue
import com.github.ixtf.core.J
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.rsocket.Closeable
import io.rsocket.ConnectionSetupPayload
import io.rsocket.RSocket
import io.rsocket.SocketAcceptor
import io.rsocket.frame.decoder.PayloadDecoder
import io.vertx.ext.auth.authentication.TokenCredentials
import io.vertx.kotlin.coroutines.coAwait
import kotlin.getValue
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

abstract class RSocketServerVerticle(
  id: String = J.objectId(),
  name: String = "RSocket",
  target: String = IXTF_API_BROKER_TARGET,
) : BaseCoroutineVerticle(), SocketAcceptor, RSocket {
  companion object {
    private val SERVER_CACHE = Caffeine.newBuilder().build<RSocketServerId, RSocketServer>()
  }

  protected open val jwtAuth by lazy { vertx.defaultAuth() }
  private val rSocketServerId = RSocketServerId(id)
  private val rSocketServer by lazy {
    SERVER_CACHE.get(rSocketServerId) { _ ->
      RSocketServer(id = rSocketServerId, target = ServerTarget(target), name = name)
    }
  }
  private lateinit var closeable: Closeable

  override suspend fun start() {
    super.start()
    closeable =
      io.rsocket.core.RSocketServer.create(this)
        .payloadDecoder(PayloadDecoder.ZERO_COPY)
        .resume(InternalKit.defaultResume(this))
        .bind(rSocketServer.target.transport())
        .awaitSingle()
  }

  override suspend fun stop() {
    closeable.dispose()
    super.stop()
  }

  override fun accept(setup: ConnectionSetupPayload, sendingSocket: RSocket): Mono<RSocket> = mono {
    val credentials = TokenCredentials(setup.readValue<String>())
    jwtAuth.authenticate(credentials).coAwait()
    this@RSocketServerVerticle
  }
}
