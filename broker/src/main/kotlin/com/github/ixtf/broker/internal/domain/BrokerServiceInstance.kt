package com.github.ixtf.broker.internal.domain

import com.github.ixtf.broker.internal.kit.remoteAddress
import io.rsocket.RSocket
import io.rsocket.util.RSocketProxy
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import java.time.Instant

internal data class BrokerServiceInstance(
  val rSocket: RSocket,
  val instance: String,
  val host: String,
  val tags: Set<String>? = null,
  val createDateTime: Instant = Instant.now(),
) : RSocketProxy(rSocket) {
  val remoteAddress by lazy { runCatching { rSocket.remoteAddress() }.getOrNull() }

  override fun toString(): String =
    json {
        obj {
          put("instance", instance)
          put("remoteAddress", remoteAddress)
          put("host", host)
          put("tags", tags)
        }
      }
      .encodePrettily()
}
