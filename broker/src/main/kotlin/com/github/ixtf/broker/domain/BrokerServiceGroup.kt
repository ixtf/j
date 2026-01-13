package com.github.ixtf.broker.domain

import com.github.ixtf.broker.domain.event.BrokerServerEvent
import java.time.Instant

data class BrokerServiceGroup(
  val id: String,
  val createDateTime: Instant,
  val modifyDateTime: Instant = createDateTime,
  val instances: List<BrokerServiceInstance> = emptyList(),
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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as BrokerServiceGroup
    return id == other.id
  }

  override fun hashCode() = id.hashCode()
}
