package com.github.ixtf.broker.internal.domain

import com.github.ixtf.broker.internal.InternalKit.remoteAddress
import io.rsocket.RSocket
import io.rsocket.util.RSocketProxy
import java.net.SocketAddress
import java.time.Instant

internal data class BrokerServiceInstance(
  val id: String,
  val rSocket: RSocket,
  val host: String,
  val tags: Set<String>? = null,
  val createDateTime: Instant = Instant.now(),
) : RSocketProxy(rSocket) {
  val remoteAddress: SocketAddress? = runCatching { rSocket.remoteAddress() }.getOrNull()

  init {
    println("remoteAddress: $remoteAddress")
  }
}
