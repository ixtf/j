package com.github.ixtf.broker

import com.github.ixtf.broker.Env.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.internal.DefaultBrokerClient
import io.vertx.core.Vertx

interface BrokerClient : NativeClient {
  fun route(route: BrokerRouteOptions): BrokerRoute

  companion object {
    fun create(vertx: Vertx, token: String, target: String = IXTF_API_BROKER_TARGET): BrokerClient =
      DefaultBrokerClient(vertx, token, target)
  }
}
