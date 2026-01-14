package com.github.ixtf.broker.internal

import com.github.ixtf.broker.BrokerRoute
import com.github.ixtf.broker.RouteOptions
import io.cloudevents.CloudEvent
import io.rsocket.Payload
import kotlinx.coroutines.flow.Flow
import reactor.core.publisher.Mono

internal class DefaultBrokerRoute(
  private val brokerClient: DefaultBrokerClient,
  private val options: RouteOptions,
) : BrokerRoute {
  override suspend fun fireAndForget(block: suspend () -> CloudEvent) {
    TODO("Not yet implemented")
  }

  override suspend fun requestResponse(block: suspend () -> CloudEvent): Mono<Payload> {
    TODO("Not yet implemented")
  }

  override suspend fun requestStream(block: suspend () -> CloudEvent): Flow<Payload> {
    TODO("Not yet implemented")
  }

  override suspend fun requestChannel(block: () -> Flow<CloudEvent>): Flow<Payload> {
    TODO("Not yet implemented")
  }

  override suspend fun metadataPush(block: suspend () -> CloudEvent) {
    TODO("Not yet implemented")
  }
}
