package com.github.ixtf.broker.verticle

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.ixtf.broker.Env.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.internal.InternalKit.defaultAuth
import com.github.ixtf.broker.internal.application.BrokerServerEntity
import com.github.ixtf.broker.internal.domain.BrokerServer
import com.github.ixtf.broker.internal.domain.BrokerServer.Companion.BrokerServerId
import com.github.ixtf.core.J
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.rsocket.RSocket
import io.rsocket.loadbalance.LoadbalanceStrategy
import io.rsocket.loadbalance.RoundRobinLoadbalanceStrategy
import io.vertx.core.ThreadingModel.VIRTUAL_THREAD
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.kotlin.core.deploymentOptionsOf
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
  protected open val lbStrategy: LoadbalanceStrategy = RoundRobinLoadbalanceStrategy()
  private val brokerServerId = BrokerServerId(id)
  private val entity by lazy {
    SERVER_CACHE.get(brokerServerId) { _ ->
      BrokerServerEntity(
        server = BrokerServer(id = brokerServerId, target = target, name = name),
        authProvider = jwtAuth,
        lbStrategy = lbStrategy,
        brokerRSocket = this,
      )
    }
  }

  override suspend fun start() {
    super.start()
    val options = deploymentOptionsOf(threadingModel = VIRTUAL_THREAD)
    vertx.deployVerticle(entity, options).coAwait()
  }
}
