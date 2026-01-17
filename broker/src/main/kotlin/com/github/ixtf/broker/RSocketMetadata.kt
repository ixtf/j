package com.github.ixtf.broker

import io.rsocket.Payload
import io.rsocket.metadata.CompositeMetadata
import io.rsocket.metadata.RoutingMetadata
import io.rsocket.metadata.WellKnownMimeType
import io.rsocket.metadata.WellKnownMimeType.MESSAGE_RSOCKET_ROUTING

open class RSocketMetadata(payload: Payload) {
  protected val metadataStore =
    if (payload.hasMetadata()) CompositeMetadata(payload.metadata(), false).toList() else null

  protected val routingMetadata by lazy {
    metadataStore
      ?.firstOrNull { it.test(MESSAGE_RSOCKET_ROUTING) }
      ?.run { RoutingMetadata(content) }
  }

  protected fun CompositeMetadata.Entry.test(wellKnownMimeType: WellKnownMimeType) =
    this is CompositeMetadata.WellKnownMimeTypeEntry && type == wellKnownMimeType
}
