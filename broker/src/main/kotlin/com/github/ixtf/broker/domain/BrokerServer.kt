package com.github.ixtf.broker.domain

import com.github.ixtf.broker.domain.event.BrokerServerEvent
import io.rsocket.RSocket
import io.rsocket.transport.ServerTransport
import io.rsocket.transport.netty.server.TcpServerTransport
import java.time.Instant

data class BrokerServer(
  val id: String,
  val name: String,
  val host: String,
  val port: Int,
  val groupMap: Map<String, ServiceGroup> = emptyMap(),
  val createDateTime: Instant = Instant.now(),
  val modifyDateTime: Instant = createDateTime,
) {
  internal fun transport(): ServerTransport<*> = TcpServerTransport.create(host, port)

  internal fun onEvent(event: BrokerServerEvent.Connected): BrokerServer {
    val group = groupMap[event.service] ?: ServiceGroup(event.service, event.fireDateTime)
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
  internal fun onEvent(event: BrokerServerEvent.Connected): ServiceGroup =
    copy(
      instances =
        instances +
          ServiceInstance(
            id = event.instance,
            rSocket = event.rSocket,
            host = event.host,
            tags = event.tags,
            createDateTime = event.fireDateTime,
          ),
      modifyDateTime = event.fireDateTime,
    )

  internal fun onEvent(event: BrokerServerEvent.DisConnected): ServiceGroup =
    copy(
      instances = instances.filter { it.id != event.instance },
      modifyDateTime = event.fireDateTime,
    )

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
  val rSocket: RSocket,
  val host: String,
  val tags: Set<String>? = null,
  val createDateTime: Instant = Instant.now(),
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as ServiceInstance
    return id == other.id
  }

  override fun hashCode() = id.hashCode()
}
