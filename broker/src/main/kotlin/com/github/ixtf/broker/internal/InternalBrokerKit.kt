package com.github.ixtf.broker.internal

import io.cloudevents.CloudEvent
import io.rsocket.Payload
import io.rsocket.util.DefaultPayload
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.time.Duration
import reactor.util.retry.Retry
import reactor.util.retry.RetryBackoffSpec

internal object InternalBrokerKit {
  internal fun defaultRetry(): RetryBackoffSpec =
    Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
      .maxBackoff(Duration.ofSeconds(3))
      .jitter(0.5)

  fun Buffer.toPayload(): Payload = DefaultPayload.create(bytes)

  fun Payload.toBuffer(): Buffer = Buffer.buffer(data().array())

  fun JsonObject.toPayload(): Payload = toBuffer().toPayload()

  fun Payload.toJsonObject(): JsonObject = toBuffer().toJsonObject()

  fun JsonArray.toPayload(): Payload = toBuffer().toPayload()

  fun JsonArray.toJsonArray(): JsonArray = toBuffer().toJsonArray()

  fun CloudEvent.toPayload(): Payload {
    TODO()
  }

  fun Payload.toCloudEvent(): CloudEvent {
    TODO()
  }
}
