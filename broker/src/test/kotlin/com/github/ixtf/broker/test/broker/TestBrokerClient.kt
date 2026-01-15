package com.github.ixtf.broker.test.broker

import com.github.ixtf.broker.BrokerClient
import com.github.ixtf.broker.BrokerClientOptions
import com.github.ixtf.broker.BrokerRouteOptions
import io.cloudevents.core.builder.CloudEventBuilder
import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.kotlin.core.vertxOptionsOf
import java.net.URI
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

private val vertx = Vertx.vertx(vertxOptionsOf(preferNativeTransport = true))
private val brokerClient = BrokerClient.create(vertx, BrokerClientOptions())
private val brokerRoute = brokerClient.route(BrokerRouteOptions("test"))

suspend fun main() {
  println("isNativeTransportEnabled: ${vertx.isNativeTransportEnabled}")

    val payload =
      brokerRoute.requestResponse {
        CloudEventBuilder.v1().withId("1").withType("test").withSource(URI("client")).build()
      }
    println(payload.dataUtf8)
}
