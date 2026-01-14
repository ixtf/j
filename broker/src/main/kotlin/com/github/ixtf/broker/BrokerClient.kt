package com.github.ixtf.broker

import com.github.ixtf.broker.internal.DefaultBrokerClient
import io.vertx.core.Vertx

interface BrokerClient : NativeClient {

  fun route(route: BrokerRouteOptions): BrokerRoute

  companion object {
    fun create(vertx: Vertx, options: BrokerClientOptions = BrokerClientOptions()): BrokerClient =
      DefaultBrokerClient(vertx, options)
  }
}
