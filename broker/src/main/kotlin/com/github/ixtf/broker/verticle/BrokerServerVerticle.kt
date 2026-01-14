package com.github.ixtf.broker.verticle

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.ixtf.broker.Env.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.internal.InternalKit.defaultAuth
import com.github.ixtf.broker.internal.application.BrokerServerEntity
import com.github.ixtf.broker.internal.domain.BrokerServer
import com.github.ixtf.core.J
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.rsocket.RSocket
import io.rsocket.loadbalance.LoadbalanceStrategy
import io.rsocket.loadbalance.RoundRobinLoadbalanceStrategy
import io.vertx.core.ThreadingModel.VIRTUAL_THREAD
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.coroutines.coAwait

private val SERVER_CACHE = Caffeine.newBuilder().build<String, BrokerServerEntity>()

abstract class BrokerServerVerticle(
  id: String = J.objectId(),
  name: String = "Broker",
  target: String = IXTF_API_BROKER_TARGET,
) : BaseCoroutineVerticle(), RSocket {
  protected open val jwtAuth by lazy { vertx.defaultAuth() }
  protected open val lbStrategy: LoadbalanceStrategy by lazy { RoundRobinLoadbalanceStrategy() }
  private val entity by lazy {
    val (host, port) = target.split(":")
    val server = BrokerServer(id = id, name = name, host = host, port = port.toInt())
    BrokerServerEntity(
      server = server,
      authProvider = jwtAuth,
      lbStrategy = lbStrategy,
      brokerRSocket = this,
    )
  }

  override suspend fun start() {
    super.start()
    val options = deploymentOptionsOf(threadingModel = VIRTUAL_THREAD)
    vertx.deployVerticle(entity, options).coAwait()
    SERVER_CACHE.put(entity.entityId, entity)
  }
}
