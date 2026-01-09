package com.github.ixtf.broker.application

import com.github.ixtf.broker.domain.event.BrokerEvent
import com.github.ixtf.broker.toCloudEvent
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.rsocket.ConnectionSetupPayload
import io.rsocket.RSocket

class BrokerEntity : BaseCoroutineVerticle() {
  suspend fun invokeSetup(setup: ConnectionSetupPayload, sendingSocket: RSocket) {
    val ce = setup.toCloudEvent()
    ce.source
    when (ce.type) {
      BrokerEvent.Registered::class.simpleName -> {}
      else -> {}
    }
    // BrokerEvent.Registered(ce.type)
    ce.data
  }
}
