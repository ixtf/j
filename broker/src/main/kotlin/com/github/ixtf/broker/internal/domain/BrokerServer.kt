package com.github.ixtf.broker.internal.domain

import com.github.ixtf.broker.internal.domain.event.BrokerServerEvent
import io.rsocket.transport.ServerTransport
import io.rsocket.transport.netty.server.TcpServerTransport
import java.time.Instant

internal data class BrokerServer(
  val id: String,
  val name: String,
  val host: String,
  val port: Int,
  val groupMap: Map<String, BrokerServiceGroup> = emptyMap(),
  val createDateTime: Instant = Instant.now(),
  val modifyDateTime: Instant = createDateTime,
) {
  internal fun transport(): ServerTransport<*> = TcpServerTransport.create(host, port)

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
