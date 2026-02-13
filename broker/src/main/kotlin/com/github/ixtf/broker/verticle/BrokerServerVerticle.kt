package com.github.ixtf.broker.verticle

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.ixtf.broker.IXTF_BROKER_TARGET
import com.github.ixtf.broker.SetupInfo
import com.github.ixtf.broker.internal.application.BrokerServerEntity
import com.github.ixtf.broker.internal.domain.BrokerServer
import com.github.ixtf.broker.internal.domain.BrokerServer.Companion.BrokerServerId
import com.github.ixtf.broker.internal.kit.ServerTarget
import com.github.ixtf.broker.internal.kit.defaultAuth
import com.github.ixtf.core.J
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.rsocket.Closeable
import io.rsocket.ConnectionSetupPayload
import io.rsocket.RSocket
import io.rsocket.SocketAcceptor
import io.rsocket.core.RSocketServer
import io.rsocket.frame.decoder.PayloadDecoder
import io.vertx.ext.auth.authentication.AuthenticationProvider
import io.vertx.ext.auth.authentication.TokenCredentials
import io.vertx.kotlin.coroutines.coAwait
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

abstract class BrokerServerVerticle(
  id: String = J.objectId(),
  name: String = "Broker",
  private val target: String = IXTF_BROKER_TARGET,
) : BaseCoroutineVerticle(), SocketAcceptor, RSocket {
  companion object {
    private val SERVER_CACHE = Caffeine.newBuilder().build<BrokerServerId, BrokerServerEntity>()
  }

  private val entity by lazy {
    SERVER_CACHE.get(BrokerServerId(id)) { id ->
      BrokerServerEntity(server = BrokerServer(id = id, name = name), brokerRSocket = this)
    }
  }
  protected open val authProvider: AuthenticationProvider by lazy { vertx.defaultAuth() }
  private lateinit var closeable: Closeable

  override fun accept(setup: ConnectionSetupPayload, sendingSocket: RSocket): Mono<RSocket> = mono {
    val credentials = TokenCredentials(setup.dataUtf8)
    val user = authProvider.authenticate(credentials).coAwait()
    val info = user.principal().mapTo(SetupInfo::class.java)
    entity.accept(info, sendingSocket)
  }

  override suspend fun start() {
    super.start()
    vertx.deployVerticle(entity).coAwait()
    closeable =
      RSocketServer.create(this)
        .payloadDecoder(PayloadDecoder.ZERO_COPY)
        // .resume(InternalKit.defaultResume(this))
        .bind(ServerTarget(target).transport())
        .awaitSingle()
    println("${javaClass.simpleName}: $target")
  }

  override suspend fun stop() {
    closeable.dispose()
    super.stop()
  }
}
