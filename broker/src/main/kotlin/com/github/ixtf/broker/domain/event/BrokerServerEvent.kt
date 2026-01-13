package com.github.ixtf.broker.domain.event

import io.rsocket.RSocket
import java.time.Instant

sealed interface BrokerServerEvent {
  data class Connected(
    val rSocket: RSocket,
    val service: String,
    val instance: String,
    val host: String,
    val tags: Set<String>? = null,
    val fireDateTime: Instant = Instant.now(),
  ) : BrokerServerEvent

  data class DisConnected(val service: String, val instance: String) : BrokerServerEvent
}
