package com.github.ixtf.broker

import io.cloudevents.CloudEvent
import io.rsocket.Payload
import kotlinx.coroutines.flow.Flow
import reactor.core.publisher.Mono

interface BrokerRoute {
  suspend fun fireAndForget(block: suspend () -> CloudEvent)

  suspend fun requestResponse(block: suspend () -> CloudEvent): Mono<Payload>

  suspend fun requestStream(block: suspend () -> CloudEvent): Flow<Payload>

  suspend fun requestChannel(block: () -> Flow<CloudEvent>): Flow<Payload>

  suspend fun metadataPush(block: suspend () -> CloudEvent)
}
