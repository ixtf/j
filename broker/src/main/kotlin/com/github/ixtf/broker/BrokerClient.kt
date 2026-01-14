package com.github.ixtf.broker

import com.github.ixtf.broker.internal.DefaultBrokerClient
import io.cloudevents.CloudEvent
import io.rsocket.Payload
import kotlinx.coroutines.flow.Flow
import reactor.core.Disposable
import reactor.core.publisher.Mono

interface BrokerClient : Disposable {
  val target: String

  suspend fun fireAndForget(block: suspend () -> CloudEvent)

  suspend fun requestResponse(block: suspend () -> CloudEvent): Mono<Payload>

  suspend fun requestStream(block: () -> Flow<CloudEvent>): Flow<Payload>

  suspend fun requestChannel(block: () -> Flow<CloudEvent>): Flow<Payload>

  suspend fun metadataPush(block: suspend () -> CloudEvent)

  companion object {
    fun create(options: BrokerClientOptions = BrokerClientOptions()): BrokerClient =
      DefaultBrokerClient(options)
  }
}
