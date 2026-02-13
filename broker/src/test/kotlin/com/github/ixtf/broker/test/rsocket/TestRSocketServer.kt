package com.github.ixtf.broker.test.rsocket

import com.github.ixtf.broker.kit.readValueAndRelease
import com.github.ixtf.broker.kit.readValueOrNull
import com.github.ixtf.broker.kit.toPayload
import com.github.ixtf.broker.verticle.RSocketMonitorVerticle
import com.github.ixtf.broker.verticle.RSocketServerVerticle
import io.cloudevents.CloudEvent
import io.rsocket.Payload
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.kotlin.core.vertxOptionsOf
import io.vertx.kotlin.coroutines.coAwait
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

// private val vertx = Vertx.vertx()
private val vertx = Vertx.vertx(vertxOptionsOf(preferNativeTransport = true))

suspend fun main() {
  vertx.deployVerticle(RSocketMonitorVerticle).coAwait()
  vertx.deployVerticle(TestRSocketServer()).coAwait()
}

private class TestRSocketServer : RSocketServerVerticle() {
  override fun requestResponse(payload: Payload): Mono<Payload> = mono {
    val ce = payload.readValueAndRelease<CloudEvent>()
    val data = ce.readValueOrNull() ?: ce.type
    val response = "requestResponse: $data"
    Buffer.buffer(response).toPayload()
  }
}
