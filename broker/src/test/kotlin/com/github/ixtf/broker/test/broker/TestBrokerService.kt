package com.github.ixtf.broker.test.broker

import com.github.ixtf.broker.readValueAndRelease
import com.github.ixtf.broker.readValueOrNull
import com.github.ixtf.broker.toPayload
import com.github.ixtf.broker.verticle.BrokerServiceVerticle
import io.cloudevents.CloudEvent
import io.rsocket.Payload
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.kotlin.core.vertxOptionsOf
import io.vertx.kotlin.coroutines.coAwait
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

private val vertx = Vertx.vertx(vertxOptionsOf(preferNativeTransport = true))

suspend fun main() {
  vertx.deployVerticle(TestBrokerService()).coAwait()

  println("isNativeTransportEnabled: ${vertx.isNativeTransportEnabled}")
}

private class TestBrokerService : BrokerServiceVerticle(service = "test", instance = "test") {
  override fun requestResponse(payload: Payload): Mono<Payload> = mono {
    val ce = payload.readValueAndRelease<CloudEvent>()
    val data = ce.readValueOrNull() ?: "requestResponse"
    Buffer.buffer(data).toPayload()
  }
}
