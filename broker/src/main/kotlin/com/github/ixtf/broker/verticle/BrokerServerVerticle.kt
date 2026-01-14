package com.github.ixtf.broker.verticle

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.ixtf.broker.Env.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.internal.InternalKit.defaultAuth
import com.github.ixtf.broker.internal.application.BrokerServerEntity
import com.github.ixtf.broker.internal.domain.BrokerServer
import com.github.ixtf.core.J
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.rsocket.Payload
import io.rsocket.RSocket
import io.vertx.core.ThreadingModel.VIRTUAL_THREAD
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.coroutines.coAwait
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private val SERVER_CACHE = Caffeine.newBuilder().build<String, BrokerServerEntity>()

abstract class BrokerServerVerticle(
  id: String = J.objectId(),
  name: String = "Broker",
  target: String = IXTF_API_BROKER_TARGET,
) : BaseCoroutineVerticle(), RSocket {
  protected open val jwtAuth by lazy { vertx.defaultAuth() }
  private val entity by lazy {
    val (host, port) = target.split(":")
    val server = BrokerServer(id = id, name = name, host = host, port = port.toInt())
    BrokerServerEntity(server = server, serverRSocket = this, authProvider = jwtAuth)
  }

  override suspend fun start() {
    super.start()
    val options = deploymentOptionsOf(threadingModel = VIRTUAL_THREAD)
    vertx.deployVerticle(entity, options).coAwait()
    SERVER_CACHE.put(entity.currentState().id, entity)
  }

  override fun metadataPush(payload: Payload): Mono<Void> {
    return super.metadataPush(payload)
  }

  override fun fireAndForget(payload: Payload): Mono<Void> {
    return super.fireAndForget(payload)
  }

  override fun requestResponse(payload: Payload): Mono<Payload> {
    return super.requestResponse(payload)
  }

  override fun requestStream(payload: Payload): Flux<Payload> {
    return super.requestStream(payload)
  }

  override fun requestChannel(payloads: Publisher<Payload>): Flux<Payload> {
    return super.requestChannel(payloads)
  }
}
