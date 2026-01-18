package com.github.ixtf.broker

import io.cloudevents.CloudEvent
import io.rsocket.Payload
import kotlinx.coroutines.flow.Flow

interface BrokerRouteClient {
  suspend fun fireAndForget(block: suspend () -> CloudEvent)

  suspend fun requestResponse(block: suspend () -> CloudEvent): Payload

  fun requestStream(block: suspend () -> CloudEvent): Flow<Payload>

  fun requestChannel(block: () -> Flow<CloudEvent>): Flow<Payload>

  suspend fun metadataPush(block: suspend () -> CloudEvent)
}
