package com.github.ixtf.broker.internal.domain

import com.github.ixtf.broker.internal.domain.event.BrokerServerEvent
import io.rsocket.loadbalance.LoadbalanceStrategy
import io.rsocket.loadbalance.RoundRobinLoadbalanceStrategy
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import java.time.Instant

internal data class BrokerServiceGroup(
  val id: String,
  val createDateTime: Instant,
  val modifyDateTime: Instant = createDateTime,
  val rSockets: List<BrokerServiceInstance> = emptyList(),
  val strategy: LoadbalanceStrategy = RoundRobinLoadbalanceStrategy(),
) {
  override fun toString(): String =
    json {
        obj {
          put("service", id)
          put("instances", rSockets)
        }
      }
      .encodePrettily()

  internal fun onEvent(event: BrokerServerEvent.Connected): BrokerServiceGroup =
    copy(
      rSockets =
        rSockets +
          BrokerServiceInstance(
            rSocket = event.rSocket,
            instance = event.instance,
            host = event.host,
            tags = event.tags,
            createDateTime = event.fireDateTime,
          ),
      modifyDateTime = event.fireDateTime,
    )

  internal fun onEvent(event: BrokerServerEvent.DisConnected): BrokerServiceGroup =
    copy(
      rSockets = rSockets.filter { it.rSocket != event.rSocket },
      modifyDateTime = event.fireDateTime,
    )
}
