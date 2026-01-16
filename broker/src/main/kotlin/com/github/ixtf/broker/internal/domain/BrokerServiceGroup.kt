package com.github.ixtf.broker.internal.domain

import cn.hutool.core.collection.CollUtil
import com.github.ixtf.broker.internal.domain.event.BrokerServerEvent
import com.github.ixtf.core.J
import io.rsocket.RSocket
import io.rsocket.loadbalance.LoadbalanceStrategy
import io.rsocket.loadbalance.RoundRobinLoadbalanceStrategy
import java.time.Instant

internal data class BrokerServiceGroup(
  val id: String,
  val createDateTime: Instant,
  val modifyDateTime: Instant = createDateTime,
  val instances: List<BrokerServiceInstance> = emptyList(),
  val lbStrategy: LoadbalanceStrategy = RoundRobinLoadbalanceStrategy(),
) {
  internal fun onEvent(event: BrokerServerEvent.Connected): BrokerServiceGroup =
    copy(
      instances =
        instances +
          BrokerServiceInstance(
            id = event.instance,
            rSocket = event.rSocket,
            host = event.host,
            tags = event.tags,
            createDateTime = event.fireDateTime,
          ),
      modifyDateTime = event.fireDateTime,
    )

  internal fun onEvent(event: BrokerServerEvent.DisConnected): BrokerServiceGroup =
    copy(
      instances = instances.filter { it.id != event.instance },
      modifyDateTime = event.fireDateTime,
    )

  internal fun pickRSocket(instance: String?, tags: List<String>?): RSocket? =
    if (J.isEmpty(instances)) null
    else if (instances.size == 1) instances.first()
    else if (instance.isNullOrBlank()) pickRSocket(tags)
    else instances.firstOrNull { it.id == instance } ?: pickRSocket(tags)

  private fun pickRSocket(tags: List<String>?): RSocket? =
    if (tags.isNullOrEmpty()) lbStrategy.select(instances)
    else
      instances.maxBy {
        val intersection = CollUtil.intersection(it.tags, tags)
        intersection.size + it.availability()
      }
}
