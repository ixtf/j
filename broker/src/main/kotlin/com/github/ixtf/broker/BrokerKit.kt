package com.github.ixtf.broker

import com.github.ixtf.vertx.readValue
import io.cloudevents.CloudEvent
import io.cloudevents.core.format.EventFormat
import io.cloudevents.core.provider.EventFormatProvider
import io.cloudevents.protobuf.ProtobufFormat.PROTO_CONTENT_TYPE
import io.rsocket.Payload
import io.rsocket.util.DefaultPayload
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

val CLOUD_EVENT_FORMAT: EventFormat by lazy {
  EventFormatProvider.getInstance().resolveFormat(PROTO_CONTENT_TYPE)!!
}

inline fun <reified T> ByteArray.readValueOrNull(): T? {
  if (isEmpty()) return null
  return when (T::class) {
    CloudEvent::class -> CLOUD_EVENT_FORMAT.deserialize(this) as T
    else -> Buffer.buffer(this).readValue()
  }
}

inline fun <reified T> CloudEvent.readValueOrNull(): T? = data?.toBytes()?.readValueOrNull()

inline fun <reified T> Payload.readValueOrNull(): T? = data().array().readValueOrNull()

inline fun <reified T> Payload.readValue(): T = requireNotNull(readValueOrNull())

fun CloudEvent.toPayload(): Payload = DefaultPayload.create(CLOUD_EVENT_FORMAT.serialize(this))

fun Buffer.toPayload(): Payload = DefaultPayload.create(bytes)

fun JsonObject.toPayload(): Payload = toBuffer().toPayload()

fun JsonArray.toPayload(): Payload = toBuffer().toPayload()

fun JsonArray.toJsonArray(): JsonArray = toBuffer().toJsonArray()
