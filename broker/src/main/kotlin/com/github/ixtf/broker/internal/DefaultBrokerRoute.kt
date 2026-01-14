package com.github.ixtf.broker.internal

import com.github.ixtf.broker.BrokerRoute
import com.github.ixtf.broker.RouteOptions
import com.github.ixtf.broker.toPayload
import io.cloudevents.CloudEvent
import io.rsocket.Payload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono

internal class DefaultBrokerRoute(
  private val brokerClient: DefaultBrokerClient,
  private val options: RouteOptions,
) : BrokerRoute {
  override suspend fun fireAndForget(block: suspend () -> CloudEvent) {
    brokerClient.fireAndForget(mono { block().toPayload() }).awaitSingleOrNull()
  }

  override suspend fun requestResponse(block: suspend () -> CloudEvent): Payload =
    brokerClient.requestResponse(mono { block().toPayload() }).awaitSingle()

  override fun requestStream(block: suspend () -> CloudEvent): Flow<Payload> =
    brokerClient.requestResponse(mono { block().toPayload() }).asFlow()

  override fun requestChannel(block: () -> Flow<CloudEvent>): Flow<Payload> {
    TODO("Not yet implemented")
  }

  override suspend fun metadataPush(block: suspend () -> CloudEvent) {
    brokerClient.metadataPush(mono { block().toPayload() }).awaitSingleOrNull()
  }
}
