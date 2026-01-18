package com.github.ixtf.broker.kit

import com.github.ixtf.vertx.kit.readValueOrNull
import io.cloudevents.CloudEvent
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.CompositeByteBuf
import io.netty.util.ReferenceCountUtil
import io.rsocket.Payload
import io.rsocket.util.DefaultPayload

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

/**
 * 为现有 Payload 追加 Metadata 并返回新 Payload。
 *
 * @param parts 要追加的新元数据片段
 */
fun Payload.mergeMetadata(vararg parts: ByteBuf): Payload {
  val allocator = ByteBufAllocator.DEFAULT
  val composite = allocator.compositeBuffer()

  return try {
    parts.forEach { composite.addComponent(true, it) }
    if (hasMetadata()) {
      val original = metadata()
      if (original is CompositeByteBuf) {
        for (i in 0 until original.numComponents()) {
          val component = original.component(i)
          composite.addComponent(true, component.slice().retain())
        }
      } else {
        composite.addComponent(true, original.slice().retain())
      }
    }
    DefaultPayload.create(data().slice().retain(), composite)
  } catch (t: Throwable) {
    // 异常清理：如果拼接失败，必须释放已经分配的资源
    ReferenceCountUtil.safeRelease(composite)
    parts.forEach { ReferenceCountUtil.safeRelease(it) }
    throw t
  } finally {
    ReferenceCountUtil.safeRelease(this)
  }
}
