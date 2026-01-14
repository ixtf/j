package com.github.ixtf.broker.test.broker

import com.github.ixtf.broker.verticle.BrokerServiceVerticle
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.coAwait

private val vertx = Vertx.vertx()

suspend fun main() {
  vertx.deployVerticle(TestBrokerService()).coAwait()

  println("success")
}

private class TestBrokerService : BrokerServiceVerticle(service = "test", instance = "test") {
  override suspend fun start() {
    super.start()
  }
}
