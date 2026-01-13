package com.github.ixtf.broker

import com.github.ixtf.broker.domain.BrokerServer
import io.rsocket.RSocket
import io.rsocket.transport.ServerTransport
import io.rsocket.transport.netty.server.TcpServerTransport
import java.time.Instant

abstract class RSocketServer : RSocket {
  abstract val id: String
  abstract val name: String
  abstract val host: String
  abstract val port: Int
  val createDateTime: Instant = Instant.now()
  val modifyDateTime: Instant = createDateTime

  protected open fun transport(): ServerTransport<*> = TcpServerTransport.create(host, port)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as BrokerServer
    return id == other.id
  }

  override fun hashCode() = id.hashCode()
}