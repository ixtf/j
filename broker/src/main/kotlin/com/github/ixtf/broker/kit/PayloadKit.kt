package com.github.ixtf.broker.kit

import com.github.ixtf.vertx.kit.readValueOrNull
import io.cloudevents.CloudEvent
import io.netty.buffer.ByteBufUtil
import io.netty.util.ReferenceCountUtil
import io.rsocket.Payload

inline fun <reified T> Payload.readValueAndRelease(): T =
  try {
    readValue()
  } finally {
    ReferenceCountUtil.safeRelease(this)
  }

inline fun <reified T> Payload.readValue(): T = requireNotNull(readValueOrNull())

inline fun <reified T> Payload.readValueOrNull(): T? {
  if (data().readableBytes() <= 0) return null
  return when (T::class) {
    CloudEvent::class -> CLOUD_EVENT_FORMAT.deserialize(ByteBufUtil.getBytes(data()))
    else -> data().readValueOrNull<T>()
  }
    as T?
}
