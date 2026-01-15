package com.github.ixtf.broker.internal.domain

import io.rsocket.RSocket
import java.time.Instant

internal data class RSocketServer(
  val id: RSocketServerId,
  val target: String,
  val name: String,
  val createDateTime: Instant = Instant.now(),
  val modifyDateTime: Instant = createDateTime,
) : RSocket {
  companion object {
    @JvmInline value class RSocketServerId(val value: String)
  }
}
