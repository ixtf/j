package com.github.ixtf.broker.internal

import com.github.ixtf.broker.BrokerClientRoute
import com.github.ixtf.broker.BrokerRouteOptions
import com.github.ixtf.broker.kit.toPayload
import io.cloudevents.CloudEvent
import io.rsocket.Payload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono

internal class DefaultBrokerClientRoute(
  private val brokerClient: DefaultBrokerClient,
  private val route: BrokerRouteOptions,
) : BrokerClientRoute {
  private fun metadata() = route.encodeMetadata()

  override suspend fun fireAndForget(block: suspend () -> CloudEvent) {
    brokerClient.fireAndForget(mono { block().toPayload(metadata()) }).awaitSingleOrNull()
  }

  override suspend fun requestResponse(block: suspend () -> CloudEvent): Payload =
    brokerClient.requestResponse(mono { block().toPayload(metadata()) }).awaitSingle()

  override fun requestStream(block: suspend () -> CloudEvent): Flow<Payload> =
    brokerClient.requestResponse(mono { block().toPayload(metadata()) }).asFlow()

  override fun requestChannel(block: () -> Flow<CloudEvent>): Flow<Payload> {
    TODO("Not yet implemented")
  }

  override suspend fun metadataPush(block: suspend () -> CloudEvent) {
    brokerClient.metadataPush(mono { block().toPayload(metadata()) }).awaitSingleOrNull()
  }
}
