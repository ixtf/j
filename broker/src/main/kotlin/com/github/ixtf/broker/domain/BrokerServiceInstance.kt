package com.github.ixtf.broker.domain

import io.rsocket.RSocket
import java.time.Instant

internal data class BrokerServiceInstance(
  val id: String,
  val rSocket: RSocket,
  val host: String,
  val tags: Set<String>? = null,
  val createDateTime: Instant = Instant.now(),
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as BrokerServiceInstance
    return id == other.id
  }

  override fun hashCode() = id.hashCode()
}
