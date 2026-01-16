package com.github.ixtf.broker

import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.CompositeByteBuf
import io.netty.util.ReferenceCountUtil
import io.rsocket.metadata.CompositeMetadataCodec.encodeAndAddMetadata
import io.rsocket.metadata.TaggingMetadataCodec.createTaggingContent
import io.rsocket.metadata.WellKnownMimeType
import java.io.Serializable

@JvmRecord
data class BrokerRouteOptions(
  val service: String? = null,
  val instance: String? = null,
  val tags: Set<String>? = null,
) : Serializable {
  fun encodeMetadata(allocator: ByteBufAllocator = ByteBufAllocator.DEFAULT): CompositeByteBuf? {
    val routing = buildList {
      service?.takeIf { it.isNotBlank() }?.let { add(it) }
      instance?.takeIf { it.isNotBlank() }?.let { add(it) }
      tags?.forEach { tag -> tag.takeIf { it.isNotBlank() }?.let { add(it) } }
    }
    if (routing.isEmpty()) return null

    val composite = allocator.compositeBuffer(8)
    try {
      val content = createTaggingContent(allocator, routing)
      encodeAndAddMetadata(composite, allocator, WellKnownMimeType.MESSAGE_RSOCKET_ROUTING, content)
      return composite
    } catch (t: Throwable) {
      ReferenceCountUtil.safeRelease(composite)
      throw t
    }
  }
}
