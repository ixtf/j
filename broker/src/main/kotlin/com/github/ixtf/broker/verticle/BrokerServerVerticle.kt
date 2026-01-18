package com.github.ixtf.broker.verticle

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.ixtf.broker.Env.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.internal.application.BrokerServerEntity
import com.github.ixtf.broker.internal.domain.BrokerServer
import com.github.ixtf.broker.internal.domain.BrokerServer.Companion.BrokerServerId
import com.github.ixtf.broker.internal.kit.ServerTarget
import com.github.ixtf.broker.internal.kit.defaultAuth
import com.github.ixtf.core.J
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.rsocket.RSocket
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.kotlin.coroutines.coAwait

abstract class BrokerServerVerticle(
  id: String = J.objectId(),
  name: String = "Broker",
  target: String = IXTF_API_BROKER_TARGET,
) : BaseCoroutineVerticle(), RSocket {
  companion object {
    private val SERVER_CACHE = Caffeine.newBuilder().build<BrokerServerId, BrokerServerEntity>()
  }

  protected open val jwtAuth: JWTAuth by lazy { vertx.defaultAuth() }
  private val entity by lazy {
    SERVER_CACHE.get(BrokerServerId(id)) { id ->
      BrokerServerEntity(
        server = BrokerServer(id = id, target = ServerTarget(target), name = name),
        authProvider = jwtAuth,
        brokerRSocket = this,
      )
    }
  }

  override suspend fun start() {
    super.start()
    vertx.deployVerticle(entity).coAwait()
  }
}
