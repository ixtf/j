package com.github.ixtf.broker

import com.github.ixtf.vertx.readValue
import io.cloudevents.CloudEvent
import io.cloudevents.core.format.EventFormat
import io.cloudevents.core.provider.EventFormatProvider
import io.cloudevents.protobuf.ProtobufFormat.PROTO_CONTENT_TYPE
import io.netty.buffer.CompositeByteBuf
import io.netty.buffer.Unpooled.wrappedBuffer
import io.rsocket.Payload
import io.rsocket.util.DefaultPayload
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

val CLOUD_EVENT_FORMAT: EventFormat by lazy {
  EventFormatProvider.getInstance().resolveFormat(PROTO_CONTENT_TYPE)!!
}

fun CloudEvent.toPayload(metadata: CompositeByteBuf?): Payload =
  CLOUD_EVENT_FORMAT.serialize(this).toPayload(metadata)

inline fun <reified T> CloudEvent.readValueOrNull(): T? = data?.toBytes()?.readValueOrNull()

fun ByteArray.toPayload(metadata: CompositeByteBuf?): Payload =
  if (metadata == null) DefaultPayload.create(this)
  else DefaultPayload.create(wrappedBuffer(this), metadata)

inline fun <reified T> ByteArray.readValueOrNull(): T? {
  if (isEmpty()) return null
  return when (T::class) {
    CloudEvent::class -> CLOUD_EVENT_FORMAT.deserialize(this) as T
    else -> Buffer.buffer(this).readValue()
  }
}

inline fun <reified T> Payload.readValueOrNull(): T? = data().array().readValueOrNull()

inline fun <reified T> Payload.readValue(): T = requireNotNull(readValueOrNull())

fun Buffer.toPayload(metadata: CompositeByteBuf?): Payload = bytes.toPayload(metadata)

fun JsonObject.toPayload(metadata: CompositeByteBuf?): Payload = toBuffer().toPayload(metadata)

fun JsonArray.toPayload(metadata: CompositeByteBuf?): Payload = toBuffer().toPayload(metadata)
