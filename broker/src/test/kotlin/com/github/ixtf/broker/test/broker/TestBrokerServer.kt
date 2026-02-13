package com.github.ixtf.broker.test.broker

import com.github.ixtf.broker.kit.readValueAndRelease
import com.github.ixtf.broker.kit.readValueOrNull
import com.github.ixtf.broker.kit.toPayload
import com.github.ixtf.broker.verticle.BrokerServerVerticle
import com.github.ixtf.broker.verticle.RSocketMonitorVerticle
import io.cloudevents.CloudEvent
import io.netty.handler.ssl.OpenSsl
import io.netty.handler.ssl.SslProvider
import io.rsocket.Payload
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.kotlin.core.vertxOptionsOf
import io.vertx.kotlin.coroutines.coAwait
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

private val vertx = Vertx.vertx(vertxOptionsOf(preferNativeTransport = true))

suspend fun main() {
  println("Is Native Transport Enabled: ${vertx.isNativeTransportEnabled}")
  println("OpenSSL Available: ${OpenSsl.isAvailable()}")
  println("ALPN Supported: ${SslProvider.isAlpnSupported(SslProvider.OPENSSL)}")
  OpenSsl.unavailabilityCause()?.printStackTrace()

  vertx.deployVerticle(RSocketMonitorVerticle).coAwait()
  vertx.deployVerticle(TestBrokerServer()).coAwait()
}

private class TestBrokerServer : BrokerServerVerticle() {
  override fun requestResponse(payload: Payload): Mono<Payload> = mono {
    val ce = payload.readValueAndRelease<CloudEvent>()
    val data = ce.readValueOrNull() ?: ce.type
    val response = "requestResponse: $data"
    Buffer.buffer(response).toPayload()
  }
}
