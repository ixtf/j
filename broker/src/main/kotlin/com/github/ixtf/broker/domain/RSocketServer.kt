package com.github.ixtf.broker.domain

import io.rsocket.RSocket
import java.time.Instant

abstract class RSocketServer(
  val id: String,
  val name: String,
  val host: String,
  val port: Int,
  val createDateTime: Instant = Instant.now(),
  val modifyDateTime: Instant = createDateTime,
) : RSocket {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as BrokerServer
    return id == other.id
  }

  override fun hashCode() = id.hashCode()
}
