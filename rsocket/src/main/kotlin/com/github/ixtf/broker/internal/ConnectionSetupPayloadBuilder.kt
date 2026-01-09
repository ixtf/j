package com.github.ixtf.broker.internal

import com.github.ixtf.broker.domain.event.BrokerEvent
import com.github.ixtf.broker.toJsonObject
import com.github.ixtf.broker.toPayload
import com.github.ixtf.core.J
import io.rsocket.ConnectionSetupPayload
import io.rsocket.Payload
import io.rsocket.RSocket
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.withContext
import reactor.core.publisher.Mono

class ConnectionSetupPayloadBuilder(val service: String, val instance: String) {
  var host: String? = null
  var token: String? = null
  var tags: Set<String>? = null

  suspend fun build(): Payload {
    val data = buildMap {
      put("service", service)
      put("instance", instance)
      host = withContext(Dispatchers.IO) { J.localIp() }
      host?.takeIf { it.isNotBlank() }?.let { put("host", it) }
      token?.takeIf { it.isNotBlank() }?.let { put("token", it) }
      tags?.takeIf { it.isNotEmpty() }?.let { put("tags", it) }
    }
    return JsonObject(data).toPayload()
  }

  companion object {
    inline fun buildConnectionSetupPayload(
      service: String,
      instance: String = J.objectId(),
      crossinline block: suspend ConnectionSetupPayloadBuilder.() -> Unit,
    ): Mono<Payload> = mono {
      val builder = ConnectionSetupPayloadBuilder(service = service, instance = instance)
      builder.block()
      builder.build()
    }

    internal fun ConnectionSetupPayload.toRegistered(sendingSocket: RSocket) =
      toJsonObject().run {
        BrokerEvent.Registered(
          sendingSocket = sendingSocket,
          service = getString("service"),
          instance = getString("instance"),
          host = getString("host"),
          tags = getJsonArray("tags")?.map { it.toString() }?.toSet(),
        )
      }
  }
}
