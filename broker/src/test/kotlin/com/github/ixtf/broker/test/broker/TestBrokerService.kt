package com.github.ixtf.broker.test.broker

import com.github.ixtf.broker.verticle.BrokerServiceVerticle
import io.rsocket.Payload
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.coAwait
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

private val vertx = Vertx.vertx()

suspend fun main() {
  vertx.deployVerticle(TestBrokerService()).coAwait()

  println("success")
}

private class TestBrokerService : BrokerServiceVerticle(service = "test", instance = "test") {
  override fun requestResponse(payload: Payload): Mono<Payload> {
    mono {}.doAfterTerminate { payload.release() }
    payload.release()
    return super.requestResponse(payload)
  }
}
