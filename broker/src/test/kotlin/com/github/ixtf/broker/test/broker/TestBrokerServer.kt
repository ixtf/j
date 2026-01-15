package com.github.ixtf.broker.test.broker

import com.github.ixtf.broker.Env.IXTF_API_BROKER_AUTH
import com.github.ixtf.broker.internal.InternalKit.defaultAuth
import com.github.ixtf.broker.verticle.BrokerServerVerticle
import io.vertx.core.Vertx
import io.vertx.kotlin.core.vertxOptionsOf
import io.vertx.kotlin.coroutines.coAwait

private val vertx = Vertx.vertx(vertxOptionsOf(preferNativeTransport = true))

suspend fun main() {
  IXTF_API_BROKER_AUTH = "test"
  vertx.deployVerticle(TestBrokerServer()).coAwait()

  vertx.defaultAuth()

  println("isNativeTransportEnabled: ${vertx.isNativeTransportEnabled}")
}

private class TestBrokerServer : BrokerServerVerticle() {}
