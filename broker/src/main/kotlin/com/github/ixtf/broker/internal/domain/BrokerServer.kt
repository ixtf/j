package com.github.ixtf.broker.internal.domain

import com.github.ixtf.broker.internal.domain.event.BrokerServerEvent
import java.time.Instant

internal data class BrokerServer(
  val id: BrokerServerId,
  val target: String,
  val name: String,
  val groupMap: Map<String, BrokerServiceGroup> = emptyMap(),
  val createDateTime: Instant = Instant.now(),
  val modifyDateTime: Instant = createDateTime,
) {
  companion object {
    @JvmInline value class BrokerServerId(val value: String)
  }

  internal fun onEvent(event: BrokerServerEvent.Connected): BrokerServer {
    val group = groupMap[event.service] ?: BrokerServiceGroup(event.service, event.fireDateTime)
    return copy(
      groupMap = groupMap + (group.id to group.onEvent(event)),
      modifyDateTime = event.fireDateTime,
    )
  }

  internal fun onEvent(event: BrokerServerEvent.DisConnected): BrokerServer {
    val group = groupMap[event.service] ?: return this
    return copy(
      groupMap = groupMap + (group.id to group.onEvent(event)),
      modifyDateTime = event.fireDateTime,
    )
  }
}
