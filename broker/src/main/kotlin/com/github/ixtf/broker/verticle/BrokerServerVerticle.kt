package com.github.ixtf.broker.verticle

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.ixtf.broker.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.application.BrokerServerEntity
import com.github.ixtf.broker.domain.BrokerServer
import com.github.ixtf.core.J
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.vertx.core.ThreadingModel.VIRTUAL_THREAD
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.coroutines.coAwait

private val SERVER_CACHE = Caffeine.newBuilder().build<String, BrokerServerEntity>()

abstract class BrokerServerVerticle : BaseCoroutineVerticle() {
  protected suspend fun addServer(
    id: String = J.objectId(),
    name: String = "Broker",
    target: String = IXTF_API_BROKER_TARGET,
  ) {
    val (host, port) = target.split(":")
    val server = BrokerServer(id = id, name = name, host = host, port = port.toInt())
    addServer(server)
  }

  protected suspend fun addServer(server: BrokerServer) {
    val entity = BrokerServerEntity(server = server)
    val options = deploymentOptionsOf(threadingModel = VIRTUAL_THREAD)
    vertx.deployVerticle(entity, options).coAwait()
    SERVER_CACHE.put(server.id, entity)
  }
}
