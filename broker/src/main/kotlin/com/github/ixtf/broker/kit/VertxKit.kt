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
 * 绕过 [io.vertx.core.buffer.impl.BufferImpl.getByteBuf] 以实现真正的零拷贝。
 * * 注意：[io.vertx.core.buffer.impl.BufferImpl.getByteBuf] 内部可能使用 Unpooled.unreleasableBuffer 包装，导致
 *   RSocket ZERO_COPY 模式下的引用计数回收失效。
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
