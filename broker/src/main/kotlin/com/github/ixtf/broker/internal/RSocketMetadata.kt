package com.github.ixtf.broker.internal

import cn.hutool.log.Log
import com.github.ixtf.core.kit.CLOUD_EVENT_FORMAT
import io.cloudevents.CloudEvent
import io.netty.buffer.ByteBufUtil
import io.netty.util.ReferenceCountUtil
import io.rsocket.Payload
import io.rsocket.metadata.CompositeMetadata
import io.rsocket.metadata.CompositeMetadata.WellKnownMimeTypeEntry
import io.rsocket.metadata.WellKnownMimeType
import io.vertx.core.Future
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.coAwait
import java.nio.charset.StandardCharsets.UTF_8
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

abstract class RSocketMetadata(private val payload: Payload) {
  private val metadataStore =
    if (payload.hasMetadata().not()) emptyList()
    else CompositeMetadata(payload.metadata(), false).toList()

  private fun CompositeMetadata.Entry.test(wellKnownMimeType: WellKnownMimeType) =
    this is WellKnownMimeTypeEntry && type == wellKnownMimeType

  protected fun hasMetadata(wellKnownMimeType: WellKnownMimeType) =
    metadataStore.firstOrNull { it.test(wellKnownMimeType) }

  fun check(wellKnownMimeType: WellKnownMimeType) = metadataStore.any { it.test(wellKnownMimeType) }

  /**
   * body 相关方法调用会自动清理底层缓冲区（避免内存泄露）
   * 1) 最佳实践，通过 metadata 判断是否可以处理请求，然后读取 body
   * 2) 如果强行读取，先调用 retain
   */
  private val bodyAsByteArrayFuture by lazy {
    Future.future { it.complete(ByteBufUtil.getBytes(payload.sliceData())) }
      .onComplete { ReferenceCountUtil.safeRelease(payload) }
      .onFailure { Log.get().error(it) }
  }

  suspend fun bodyAsByteArray(): ByteArray = mono { bodyAsByteArrayFuture.coAwait() }.awaitSingle()

  suspend fun bodyAsString(): String = bodyAsBuffer().toString(UTF_8)

  suspend fun bodyAsBuffer(): Buffer = Buffer.buffer(bodyAsByteArray())

  suspend fun bodyAsJsonArray(): JsonArray = bodyAsBuffer().toJsonArray()

  suspend fun bodyAsJsonObject(): JsonObject = bodyAsBuffer().toJsonObject()

  suspend fun bodyAsCloudEvent(): CloudEvent = CLOUD_EVENT_FORMAT.deserialize(bodyAsByteArray())

  fun <T : Any> bodyAsByteArray(block: suspend (ByteArray) -> T): Mono<T> = mono {
    block(bodyAsByteArrayFuture.coAwait())
  }

  fun <T : Any> bodyAsString(block: suspend (String) -> T): Mono<T> = bodyAsBuffer {
    block(it.toString(UTF_8))
  }

  fun <T : Any> bodyAsBuffer(block: suspend (Buffer) -> T): Mono<T> = bodyAsByteArray {
    block(Buffer.buffer(it))
  }

  fun <T : Any> bodyAsJsonArray(block: suspend (JsonArray) -> T): Mono<T> = bodyAsBuffer {
    block(it.toJsonArray())
  }

  fun <T : Any> bodyAsJsonObject(block: suspend (JsonObject) -> T): Mono<T> = bodyAsBuffer {
    block(it.toJsonObject())
  }

  fun <T : Any> bodyAsCloudEvent(block: suspend (CloudEvent) -> T): Mono<T> = bodyAsByteArray {
    block(CLOUD_EVENT_FORMAT.deserialize(it))
  }
}
