package com.github.ixtf.broker.kit

import com.github.ixtf.vertx.kit.readValueOrNull
import io.cloudevents.CloudEvent
import io.netty.util.ReferenceCountUtil
import io.rsocket.Payload

inline fun <reified T> Payload.readValueAndRelease(): T =
  try {
    readValue()
  } finally {
    ReferenceCountUtil.safeRelease(this)
  }

inline fun <reified T> Payload.readValue(): T =
  requireNotNull(readValueOrNull()) { "Payload.readValue<[${T::class.java}]>()" }

inline fun <reified T> Payload.readValueOrNull(): T? {
  val byteBuf = data()
  if (byteBuf.readableBytes() <= 0) return null
  return when (T::class) {
    CloudEvent::class -> byteBuf.readValueOrNull<ByteArray>()?.let(CLOUD_EVENT_FORMAT::deserialize)
    else -> byteBuf.readValueOrNull<T>()
  }
    as T?
}
