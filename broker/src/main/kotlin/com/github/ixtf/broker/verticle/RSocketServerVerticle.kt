package com.github.ixtf.broker.verticle

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.ixtf.broker.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.application.BrokerServerEntity
import com.github.ixtf.core.J
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle

private val SERVER_CACHE = Caffeine.newBuilder().build<String, BrokerServerEntity>()

abstract class RSocketServerVerticle : BaseCoroutineVerticle() {
  protected suspend fun addRSocketServer(
    id: String = J.objectId(),
    name: String = "RSocket",
    target: String = IXTF_API_BROKER_TARGET,
  ) {}
}
