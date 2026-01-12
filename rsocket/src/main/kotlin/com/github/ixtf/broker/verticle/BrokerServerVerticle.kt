package com.github.ixtf.broker.verticle

import com.github.ixtf.broker.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.domain.BrokerServer
import com.github.ixtf.broker.domain.event.BrokerServerEvent
import com.github.ixtf.broker.toCloudEvent
import com.github.ixtf.core.J
import com.github.ixtf.vertx.verticle.ActorVerticle
import io.rsocket.ConnectionSetupPayload
import io.rsocket.RSocket

abstract class BrokerServerVerticle(
  private val id: String = J.objectId(),
  private val name: String = "Broker",
  private val target: String = IXTF_API_BROKER_TARGET,
) : ActorVerticle<BrokerServer, BrokerServerEvent>() {
  override suspend fun persist(entity: BrokerServer) = Unit

  override suspend fun emptyState(): BrokerServer {
    val (host, port) = target.split(":")
    return BrokerServer(id = id, name = name, host = host, port = port.toInt())
  }

  override suspend fun applyEvent(event: BrokerServerEvent): BrokerServer =
    when (event) {
      is BrokerServerEvent.Registered -> currentState().onEvent(event)
    }

  suspend fun invokeSetup(setup: ConnectionSetupPayload, sendingSocket: RSocket) {
    val ce = setup.toCloudEvent()
    ce.source
    when (ce.type) {
      BrokerServerEvent.Registered::class.simpleName -> {}
      else -> {}
    }
    // BrokerEvent.Registered(ce.type)
    ce.data
  }
}
