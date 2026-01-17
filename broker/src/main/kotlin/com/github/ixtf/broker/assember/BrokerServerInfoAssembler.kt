package com.github.ixtf.broker.assember

import com.github.ixtf.broker.internal.domain.BrokerServer
import com.github.ixtf.broker.internal.domain.BrokerServiceGroup
import com.github.ixtf.broker.internal.domain.BrokerServiceInstance
import java.time.Instant

data class BrokerServerInfo(
  val id: String,
  val services: Collection<BrokerServiceInfo>,
  val createDateTime: Instant,
  val modifyDateTime: Instant,
  val name: String,
  val target: String,
) {
  internal constructor(
    brokerServer: BrokerServer
  ) : this(
    id = brokerServer.id.value,
    services = brokerServer.groupMap.values.map { BrokerServiceInfo(it) },
    createDateTime = brokerServer.createDateTime,
    modifyDateTime = brokerServer.modifyDateTime,
    name = brokerServer.name,
    target = brokerServer.target.value,
  )
}

data class BrokerServiceInfo(
  val id: String,
  val instances: Collection<BrokerServiceInstanceInfo>,
  val createDateTime: Instant,
  val modifyDateTime: Instant,
) {
  internal constructor(
    group: BrokerServiceGroup
  ) : this(
    id = group.id,
    instances = group.rSockets.map { BrokerServiceInstanceInfo(it) },
    createDateTime = group.createDateTime,
    modifyDateTime = group.modifyDateTime,
  )
}

data class BrokerServiceInstanceInfo(
  val instance: String,
  val remoteAddress: String,
  val host: String,
  val tags: Set<String>?,
  val createDateTime: Instant,
) {
  internal constructor(
    instance: BrokerServiceInstance
  ) : this(
    instance = instance.instance,
    remoteAddress = "${instance.remoteAddress}",
    host = instance.host,
    tags = instance.tags,
    createDateTime = instance.createDateTime,
  )
}
