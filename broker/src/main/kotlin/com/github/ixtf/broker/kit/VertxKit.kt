package com.github.ixtf.broker.kit

import io.netty.buffer.CompositeByteBuf
import io.netty.buffer.Unpooled.wrappedBuffer
import io.rsocket.Payload
import io.rsocket.util.DefaultPayload
import io.vertx.core.buffer.Buffer
import io.vertx.core.internal.buffer.BufferInternal
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

fun ByteArray.toPayload(metadata: CompositeByteBuf? = null): Payload =
  if (metadata == null) DefaultPayload.create(wrappedBuffer(this))
  else DefaultPayload.create(wrappedBuffer(this), metadata)

// val dataBuf = (this as? BufferInternal)?.byteBuf()?.copy()
fun Buffer.toPayload(metadata: CompositeByteBuf? = null): Payload {
  val internal = this as? BufferInternal
  val dataBuf = internal?.byteBuf ?: wrappedBuffer(bytes)
  //
  // val dataBuf = internal?.byteBuf?.copy() ?: wrappedBuffer(bytes)
  return if (metadata == null) DefaultPayload.create(dataBuf)
  else DefaultPayload.create(dataBuf, metadata)
}

fun JsonObject.toPayload(metadata: CompositeByteBuf? = null): Payload =
  toBuffer().toPayload(metadata)

fun JsonArray.toPayload(metadata: CompositeByteBuf? = null): Payload =
  toBuffer().toPayload(metadata)
