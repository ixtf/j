package com.github.ixtf.broker.internal.application

import cn.hutool.core.collection.CollUtil
import com.github.ixtf.broker.internal.domain.BrokerServer
import io.rsocket.Payload
import io.rsocket.RSocket
import io.rsocket.loadbalance.LoadbalanceStrategy
import io.rsocket.metadata.CompositeMetadata
import io.rsocket.metadata.RoutingMetadata
import io.rsocket.metadata.WellKnownMimeType
import kotlin.collections.get

internal class BrokerContext(private val server: BrokerServer, payload: Payload) {
  private val metadataStore =
    if (payload.hasMetadata().not()) emptyList()
    else CompositeMetadata(payload.metadata(), false).toList()

  private val routingMetadata by lazy {
    metadataStore
      .firstOrNull { it.test(WellKnownMimeType.MESSAGE_RSOCKET_ROUTING) }
      ?.run { RoutingMetadata(content) }
  }

  private val service by lazy { routingMetadata?.first() }
  private val tags by lazy { routingMetadata?.drop(1) }
  // 可能是 tag，需要结合 BrokerService 自行 check
  private val instance by lazy { tags?.first() }

  private fun CompositeMetadata.Entry.test(wellKnownMimeType: WellKnownMimeType) =
    this is CompositeMetadata.WellKnownMimeTypeEntry && type == wellKnownMimeType

  internal fun pickRSocket(lbStrategy: LoadbalanceStrategy, brokerRSocket: RSocket): RSocket {
    if (service.isNullOrBlank()) return brokerRSocket
    val rSockets = server.groupMap[service]?.instances
    require(rSockets.isNullOrEmpty().not())
    if (tags.isNullOrEmpty()) return lbStrategy.select(rSockets)
    return rSockets.firstOrNull { it.id == instance }
      ?: rSockets.maxBy {
        val intersection = CollUtil.intersection(it.tags, tags)
        intersection.size + it.availability()
      }
  }
}
