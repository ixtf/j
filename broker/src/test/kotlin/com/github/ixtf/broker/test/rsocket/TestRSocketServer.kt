package com.github.ixtf.broker.test.rsocket

import com.github.ixtf.broker.verticle.RSocketServerVerticle
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.coAwait

private val vertx = Vertx.vertx()

suspend fun main() {
  vertx.deployVerticle(TestRSocketServer()).coAwait()
}

private class TestRSocketServer : RSocketServerVerticle() {}
