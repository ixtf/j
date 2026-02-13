package com.github.ixtf.broker.internal.domain.event

import com.fasterxml.jackson.annotation.JsonIgnore
import io.rsocket.RSocket
import java.time.Instant

sealed interface BrokerServerEvent {
  data class Connected(
    val service: String,
    @JsonIgnore val rSocket: RSocket,
    val instance: String,
    val host: String,
    val tags: Set<String>? = null,
    val fireDateTime: Instant = Instant.now(),
  ) : BrokerServerEvent

  data class DisConnected(
    val service: String,
    val rSocket: RSocket,
    val fireDateTime: Instant = Instant.now(),
  ) : BrokerServerEvent
}
