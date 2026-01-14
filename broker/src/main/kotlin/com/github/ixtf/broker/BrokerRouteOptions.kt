package com.github.ixtf.broker

import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.CompositeByteBuf
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
  companion object {
    private fun CompositeByteBuf.routing(routing: Collection<String>) = apply {
      if (routing.isNotEmpty()) {
        val content = createTaggingContent(ByteBufAllocator.DEFAULT, routing)
        encodeAndAddMetadata(
          this,
          ByteBufAllocator.DEFAULT,
          WellKnownMimeType.MESSAGE_RSOCKET_ROUTING,
          content,
        )
      }
    }
  }

  fun routing(): CompositeByteBuf? {
    val routing = buildList {
      service?.takeIf { it.isNotBlank() }?.let { add(it) }
      instance?.takeIf { it.isNotBlank() }?.let { add(it) }
      tags?.forEach { tag -> tag.takeIf { it.isNotBlank() }?.let { add(it) } }
    }
    if (routing.isEmpty()) return null
    return CompositeByteBuf(ByteBufAllocator.DEFAULT, false, 8).routing(routing)
  }
}
