package com.github.ixtf.broker

import com.github.ixtf.broker.internal.DefaultBrokerClient
import io.cloudevents.CloudEvent
import io.rsocket.Payload
import io.vertx.core.Vertx
import kotlinx.coroutines.flow.Flow
import reactor.core.Disposable
import reactor.core.publisher.Mono

interface BrokerClient : Disposable {
  val target: String

  suspend fun fireAndForget(block: suspend () -> CloudEvent)

  suspend fun requestResponse(block: suspend () -> CloudEvent): Mono<Payload>

  suspend fun requestStream(block: suspend () -> CloudEvent): Flow<Payload>

  suspend fun requestChannel(block: () -> Flow<CloudEvent>): Flow<Payload>

  suspend fun metadataPush(block: suspend () -> CloudEvent)

  companion object {
    fun create(vertx: Vertx, options: BrokerClientOptions = BrokerClientOptions()): BrokerClient =
      DefaultBrokerClient(vertx, options)
  }
}
