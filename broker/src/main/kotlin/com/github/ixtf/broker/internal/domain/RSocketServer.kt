package com.github.ixtf.broker.internal.domain

import io.rsocket.RSocket
import io.rsocket.transport.ServerTransport
import io.rsocket.transport.netty.server.TcpServerTransport
import java.time.Instant

internal data class RSocketServer(
  val id: String,
  val name: String,
  val host: String,
  val port: Int,
  val createDateTime: Instant = Instant.now(),
  val modifyDateTime: Instant = createDateTime,
) : RSocket {
  internal fun transport(): ServerTransport<*> = TcpServerTransport.create(host, port)
}
