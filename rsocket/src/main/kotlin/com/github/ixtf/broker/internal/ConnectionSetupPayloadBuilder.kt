package com.github.ixtf.broker.internal

import com.github.ixtf.broker.toPayload
import com.github.ixtf.core.J
import io.rsocket.Payload
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.withContext
import reactor.core.publisher.Mono

class ConnectionSetupPayloadBuilder(val service: String, val instance: String) {
  var token: String? = null
  var tags: Set<String>? = null

  suspend fun build(): Payload {
    val data = buildMap {
      put("host", withContext(Dispatchers.IO) { J.localIp() })
      token?.takeIf { it.isNotBlank() }?.let { put("token", it) }
      tags?.takeIf { it.isNotEmpty() }?.let { put("tags", it) }
    }
    return JsonObject(data).toPayload()
  }

  companion object {
    inline fun buildConnectionSetupPayload(
      service: String,
      instance: String,
      crossinline block: suspend ConnectionSetupPayloadBuilder.() -> Unit,
    ): Mono<Payload> = mono {
      val builder = ConnectionSetupPayloadBuilder(service = service, instance = instance)
      builder.block()
      builder.build()
    }
  }
}
