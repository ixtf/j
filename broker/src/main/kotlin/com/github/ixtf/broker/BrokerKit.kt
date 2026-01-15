package com.github.ixtf.broker

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.ixtf.core.MAPPER
import com.github.ixtf.vertx.readValue
import io.cloudevents.CloudEvent
import io.cloudevents.core.format.EventFormat
import io.cloudevents.core.provider.EventFormatProvider
import io.cloudevents.protobuf.ProtobufFormat.PROTO_CONTENT_TYPE
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.CompositeByteBuf
import io.netty.buffer.Unpooled.wrappedBuffer
import io.netty.util.ReferenceCountUtil
import io.rsocket.Payload
import io.rsocket.util.DefaultPayload
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.nio.charset.StandardCharsets

inline fun <reified T> Payload.readValue(): T = requireNotNull(readValueOrNull())

inline fun <reified T> Payload.readValueOrNull(): T? =
  try {
    data().readValueOrNull()
  } finally {
    ReferenceCountUtil.safeRelease(this)
  }

inline fun <reified T> ByteBuf.readValueOrNull(): T? =
  if (readableBytes() <= 0) null
  else
    when (T::class) {
      CloudEvent::class -> CLOUD_EVENT_FORMAT.deserialize(ByteBufUtil.getBytes(this))
      String::class -> toString(StandardCharsets.UTF_8)
      ByteArray::class -> ByteBufUtil.getBytes(this)
      Buffer::class -> Buffer.buffer(ByteBufUtil.getBytes(this))
      JsonObject::class -> Buffer.buffer(ByteBufUtil.getBytes(this)).toJsonObject()
      JsonArray::class -> Buffer.buffer(ByteBufUtil.getBytes(this)).toJsonArray()
      else -> ByteBufInputStream(this).use { MAPPER.readValue<T>(it) }
    }
      as T

val CLOUD_EVENT_FORMAT: EventFormat by lazy {
  EventFormatProvider.getInstance().resolveFormat(PROTO_CONTENT_TYPE)!!
}

fun CloudEvent.toPayload(metadata: CompositeByteBuf?): Payload =
  CLOUD_EVENT_FORMAT.serialize(this).toPayload(metadata)

inline fun <reified T> CloudEvent.readValueOrNull(): T? =
  data?.toBytes()?.let { Buffer.buffer(it).readValue() }

fun ByteArray.toPayload(metadata: CompositeByteBuf?): Payload =
  if (metadata == null) DefaultPayload.create(this)
  else DefaultPayload.create(wrappedBuffer(this), metadata)

fun Buffer.toPayload(metadata: CompositeByteBuf?): Payload = bytes.toPayload(metadata)

fun JsonObject.toPayload(metadata: CompositeByteBuf?): Payload = toBuffer().toPayload(metadata)

fun JsonArray.toPayload(metadata: CompositeByteBuf?): Payload = toBuffer().toPayload(metadata)
