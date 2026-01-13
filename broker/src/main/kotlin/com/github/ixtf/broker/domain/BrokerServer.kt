package com.github.ixtf.broker.domain

import com.github.ixtf.broker.domain.event.BrokerServerEvent
import io.rsocket.RSocket

data class BrokerServer(val id: String, val name: String, val host: String, val port: Int) :
  RSocket {
  fun onEvent(event: BrokerServerEvent.Registered): BrokerServer {
    TODO("Not yet implemented")
  }
}
