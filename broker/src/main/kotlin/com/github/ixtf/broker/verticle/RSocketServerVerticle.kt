package com.github.ixtf.broker.verticle

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.ixtf.broker.Env.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.application.BrokerServerEntity
import com.github.ixtf.broker.internal.defaultBrokerAuthProvider
import com.github.ixtf.core.J
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.rsocket.RSocket

private val SERVER_CACHE = Caffeine.newBuilder().build<String, BrokerServerEntity>()

abstract class RSocketServerVerticle : BaseCoroutineVerticle(), RSocket {
  protected open val id: String = J.objectId()
  protected open val name: String = "Broker"
  protected open val target: String = IXTF_API_BROKER_TARGET
  protected open val authProvider by lazy { vertx.defaultBrokerAuthProvider() }

  protected suspend fun addRSocketServer(
    id: String = J.objectId(),
    name: String = "RSocket",
    target: String = IXTF_API_BROKER_TARGET,
  ) {}
}
