package com.github.ixtf.broker.internal.domain

import com.github.ixtf.broker.internal.domain.event.BrokerServerEvent
import io.rsocket.loadbalance.LoadbalanceStrategy
import io.rsocket.loadbalance.RoundRobinLoadbalanceStrategy
import java.time.Instant

internal data class BrokerServiceGroup(
  val id: String,
  val createDateTime: Instant,
  val modifyDateTime: Instant = createDateTime,
  val instances: List<BrokerServiceInstance> = emptyList(),
  val lbStrategy: LoadbalanceStrategy = RoundRobinLoadbalanceStrategy(),
) {
  internal fun onEvent(event: BrokerServerEvent.Connected): BrokerServiceGroup =
    copy(
      instances =
        instances +
          BrokerServiceInstance(
            id = event.instance,
            rSocket = event.rSocket,
            host = event.host,
            tags = event.tags,
            createDateTime = event.fireDateTime,
          ),
      modifyDateTime = event.fireDateTime,
    )

  internal fun onEvent(event: BrokerServerEvent.DisConnected): BrokerServiceGroup =
    copy(
      instances = instances.filter { it.id != event.instance },
      modifyDateTime = event.fireDateTime,
    )
}
