package com.github.ixtf.broker.kit

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.ixtf.core.MAPPER
import io.cloudevents.CloudEvent
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufUtil
import io.netty.util.ReferenceCountUtil
import io.rsocket.Payload
import io.vertx.core.buffer.Buffer
import io.vertx.core.internal.buffer.BufferInternal
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.nio.charset.StandardCharsets

inline fun <reified T> Payload.readValueAndRelease(): T =
  try {
    readValue()
  } finally {
    ReferenceCountUtil.safeRelease(this)
  }

inline fun <reified T> Payload.readValue(): T = requireNotNull(readValueOrNull())

inline fun <reified T> Payload.readValueOrNull(): T? = data().readValueOrNull()

/** 支持重复读取的解析工具 使用 [slice] 确保不移动原始 ByteBuf 的 readerIndex */
inline fun <reified T> ByteBuf.readValueOrNull(): T? =
  if (readableBytes() <= 0) null
  else
    when (T::class) {
      CloudEvent::class -> CLOUD_EVENT_FORMAT.deserialize(ByteBufUtil.getBytes(this))
      String::class -> toString(StandardCharsets.UTF_8)
      ByteArray::class -> ByteBufUtil.getBytes(this)
      // {@link io.vertx.core.buffer.impl.BufferImpl#getByteBuf}
      Buffer::class -> BufferInternal.buffer(slice())
      JsonObject::class -> BufferInternal.buffer(slice()).toJsonObject()
      JsonArray::class -> BufferInternal.buffer(slice()).toJsonArray()
      else -> ByteBufInputStream(this).use { MAPPER.readValue<T>(it) }
    }
      as T
