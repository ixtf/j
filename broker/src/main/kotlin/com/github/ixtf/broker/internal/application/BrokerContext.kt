package com.github.ixtf.broker.internal.application

import cn.hutool.core.collection.CollUtil
import com.github.ixtf.broker.RSocketMetadata
import com.github.ixtf.broker.internal.domain.BrokerServer
import com.github.ixtf.broker.internal.domain.BrokerServiceGroup
import io.rsocket.Payload
import io.rsocket.RSocket

internal class BrokerContext(private val server: BrokerServer, payload: Payload) :
  RSocketMetadata(payload) {
  private val service by lazy { routingMetadata?.firstOrNull() }
  private val tags by lazy { routingMetadata?.drop(1) }
  // 可能是 tag，需要结合 BrokerService 自行 check
  private val instance by lazy { tags?.firstOrNull() }

  internal fun pickRSocketOrNull(brokerRSocket: RSocket): RSocket? =
    if (service.isNullOrBlank()) brokerRSocket else server.groupMap[service]?.pickRSocketOrNull()

  private fun BrokerServiceGroup.pickRSocketOrNull(): RSocket? =
    when {
      instances.isEmpty() -> null
      instances.size == 1 -> instances.first()
      tags.isNullOrEmpty() -> lbStrategy.select(instances)
      else ->
        instances.firstOrNull { it.id == instance }
          ?: instances.maxBy {
            val intersection = CollUtil.intersection(it.tags, tags)
            intersection.size + it.availability()
          }
    }
}
