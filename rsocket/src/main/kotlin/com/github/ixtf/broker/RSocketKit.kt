package com.github.ixtf.broker

import io.cloudevents.CloudEvent
import io.rsocket.Payload
import io.rsocket.util.DefaultPayload
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

fun Buffer.toPayload(): Payload = DefaultPayload.create(bytes)

fun JsonObject.toPayload(): Payload = toBuffer().toPayload()

fun JsonArray.toPayload(): Payload = toBuffer().toPayload()

fun CloudEvent.toPayload(): Payload {
  TODO()
}

fun Payload.toCloudEvent(): CloudEvent {
  TODO()
}
