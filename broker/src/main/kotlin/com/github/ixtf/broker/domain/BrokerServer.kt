package com.github.ixtf.broker.domain

import com.github.ixtf.broker.domain.event.BrokerServerEvent
import io.rsocket.RSocket
import java.time.Instant

data class BrokerServer(
  val id: String,
  val name: String,
  val host: String,
  val port: Int,
  val groupMap: Map<String, ServiceGroup> = emptyMap(),
  val createDateTime: Instant = Instant.now(),
  val modifyDateTime: Instant = createDateTime,
) : RSocket {
  internal fun onEvent(event: BrokerServerEvent.Connected): BrokerServer {
    val group = groupMap[event.service] ?: ServiceGroup(event.service, event.fireDateTime)
    return copy(
      groupMap = groupMap + (group.id to group.onEvent(event)),
      modifyDateTime = event.fireDateTime,
    )
  }

  internal fun onEvent(event: BrokerServerEvent.DisConnected): BrokerServer {
    val group = groupMap[event.service] ?: return this
    group.onEvent(event)
    TODO("Not yet implemented")
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as BrokerServer
    return id == other.id
  }

  override fun hashCode() = id.hashCode()
}

data class ServiceGroup(
  val id: String,
  val createDateTime: Instant,
  val modifyDateTime: Instant = createDateTime,
  val instances: List<ServiceInstance> = emptyList(),
) {
  internal fun onEvent(event: BrokerServerEvent.Connected) =
    copy(
      instances =
        instances +
          ServiceInstance(
            id = event.instance,
            service = event.service,
            rSocket = event.rSocket,
            tags = event.tags,
            createDateTime = event.fireDateTime,
          ),
      modifyDateTime = event.fireDateTime,
    )

  internal fun onEvent(event: BrokerServerEvent.DisConnected) {
    TODO("Not yet implemented")
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as ServiceGroup
    return id == other.id
  }

  override fun hashCode() = id.hashCode()
}

data class ServiceInstance(
  val id: String,
  val service: String,
  val rSocket: RSocket,
  val tags: Set<String>? = null,
  val createDateTime: Instant = Instant.now(),
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as ServiceInstance
    if (service != other.service) return false
    if (id != other.id) return false
    return true
  }

  override fun hashCode(): Int {
    var result = service.hashCode()
    result = 31 * result + id.hashCode()
    return result
  }
}
