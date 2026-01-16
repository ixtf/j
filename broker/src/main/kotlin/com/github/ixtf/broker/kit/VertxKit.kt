package com.github.ixtf.broker.kit

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled.wrappedBuffer
import io.rsocket.Payload
import io.rsocket.util.DefaultPayload
import io.vertx.core.buffer.Buffer
import io.vertx.core.internal.buffer.BufferInternal
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

fun ByteArray.toPayload(metadata: ByteBuf? = null): Payload =
  if (metadata == null) DefaultPayload.create(wrappedBuffer(this))
  else DefaultPayload.create(wrappedBuffer(this), metadata)

/**
 * [io.vertx.core.buffer.impl.BufferImpl.getByteBuf]
 *
 * 有可能出现 Unpooled.unreleasableBuffer 这会导致 RSocket 的 ZERO_COPY 机制在尝试回收内存时失效，进而引发 Direct Memory (堆外内存)
 * 泄漏 可以通过 internal?.byteBuf?.copy() 再复制一次
 */
fun Buffer.toPayload(metadata: ByteBuf? = null): Payload {
  val internal = this as? BufferInternal
  val dataBuf = internal?.byteBuf ?: wrappedBuffer(bytes)
  // val dataBuf = internal?.byteBuf?.copy() ?: wrappedBuffer(bytes)
  return if (metadata == null) DefaultPayload.create(dataBuf)
  else DefaultPayload.create(dataBuf, metadata)
}

fun JsonObject.toPayload(metadata: ByteBuf? = null): Payload = toBuffer().toPayload(metadata)

fun JsonArray.toPayload(metadata: ByteBuf? = null): Payload = toBuffer().toPayload(metadata)
