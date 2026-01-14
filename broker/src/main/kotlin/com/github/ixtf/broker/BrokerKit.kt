package com.github.ixtf.broker

import com.github.ixtf.vertx.readValue
import io.cloudevents.CloudEvent
import io.rsocket.Payload
import io.rsocket.util.DefaultPayload
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

fun Payload.toBuffer(): Buffer = Buffer.buffer(data().array())

inline fun <reified T> Payload.readValue(): T = toBuffer().readValue()

fun Payload.toJsonObject(): JsonObject = toBuffer().toJsonObject()

fun Buffer.toPayload(): Payload = DefaultPayload.create(bytes)

fun JsonObject.toPayload(): Payload = toBuffer().toPayload()

fun JsonArray.toPayload(): Payload = toBuffer().toPayload()

fun JsonArray.toJsonArray(): JsonArray = toBuffer().toJsonArray()

fun CloudEvent.toPayload(): Payload {
  TODO()
}

fun Payload.toCloudEvent(): CloudEvent {
  TODO()
}
