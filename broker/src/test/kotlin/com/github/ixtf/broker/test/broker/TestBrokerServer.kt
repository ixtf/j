package com.github.ixtf.broker.test.broker

import com.github.ixtf.broker.verticle.BrokerServerVerticle
import com.github.ixtf.broker.verticle.RSocketMonitorVerticle
import io.vertx.core.Vertx
import io.vertx.kotlin.core.vertxOptionsOf
import io.vertx.kotlin.coroutines.coAwait

private val vertx = Vertx.vertx(vertxOptionsOf(preferNativeTransport = true))

suspend fun main() {
  vertx.deployVerticle(TestBrokerServer()).coAwait()
  vertx.deployVerticle(RSocketMonitorVerticle).coAwait()
}

private class TestBrokerServer : BrokerServerVerticle() {}
