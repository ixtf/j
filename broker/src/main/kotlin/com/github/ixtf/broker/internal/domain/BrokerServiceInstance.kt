package com.github.ixtf.broker.internal.domain

import cn.hutool.log.Log
import com.github.ixtf.broker.internal.kit.remoteAddress
import io.rsocket.RSocket
import io.rsocket.util.RSocketProxy
import java.time.Instant

internal data class BrokerServiceInstance(
  val id: String,
  val rSocket: RSocket,
  val host: String,
  val tags: Set<String>? = null,
  val createDateTime: Instant = Instant.now(),
) : RSocketProxy(rSocket) {
  val remoteAddress by lazy { runCatching { rSocket.remoteAddress() }.getOrNull() }

  init {
    Log.get().info("remoteAddress: {}", remoteAddress)
  }
}
