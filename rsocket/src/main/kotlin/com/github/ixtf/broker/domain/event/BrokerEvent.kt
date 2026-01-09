package com.github.ixtf.broker.domain.event

import io.rsocket.RSocket
import java.time.Instant

sealed interface BrokerEvent {
  data class Registered(
    val sendingSocket: RSocket,
    val service: String,
    val instance: String,
    val host: String,
    val tags: Set<String>? = null,
    val fireDateTime: Instant = Instant.now(),
  ) : BrokerEvent
}
