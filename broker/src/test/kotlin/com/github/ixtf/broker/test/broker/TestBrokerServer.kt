package com.github.ixtf.broker.test.broker

import com.github.ixtf.broker.verticle.BrokerServerVerticle
import io.vertx.core.Vertx
import io.vertx.kotlin.core.vertxOptionsOf
import io.vertx.kotlin.coroutines.coAwait

private val vertx = Vertx.vertx(vertxOptionsOf(preferNativeTransport = true))

suspend fun main() {
  vertx.deployVerticle(TestBrokerServer()).coAwait()

  println("isNativeTransportEnabled: ${vertx.isNativeTransportEnabled}")
}

private class TestBrokerServer : BrokerServerVerticle() {}
