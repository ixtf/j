package com.github.ixtf.broker.internal.domain

import com.github.ixtf.broker.internal.ServerTarget
import io.rsocket.RSocket
import java.time.Instant

internal data class RSocketServer(
  val id: RSocketServerId,
  val target: ServerTarget,
  val name: String,
  val createDateTime: Instant = Instant.now(),
  val modifyDateTime: Instant = createDateTime,
) : RSocket {
  companion object {
    @JvmInline value class RSocketServerId(val value: String)
  }
}
