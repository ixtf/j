package com.github.ixtf.broker.test.broker

import com.github.ixtf.broker.BrokerClient
import com.github.ixtf.broker.BrokerClientOptions
import com.github.ixtf.broker.BrokerRouteOptions
import com.github.ixtf.broker.readValueAndRelease
import com.github.ixtf.core.J
import io.cloudevents.core.builder.CloudEventBuilder
import io.vertx.core.Vertx
import io.vertx.kotlin.core.vertxOptionsOf
import java.net.URI
import java.time.OffsetDateTime
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val vertx = Vertx.vertx(vertxOptionsOf(preferNativeTransport = true))
private val brokerClient = BrokerClient.create(vertx, BrokerClientOptions())
private val brokerRoute = brokerClient.route(BrokerRouteOptions("test"))

fun main() {
  runBlocking {
    println("isNativeTransportEnabled: ${vertx.isNativeTransportEnabled}")

    launch { requestResponse("test") }
    launch { requestResponse("other1") }
    launch { requestResponse("other2") }
    launch { requestResponse("other3") }
  }
}

private suspend fun requestResponse(type: String) {
  val payload =
    brokerRoute.requestResponse {
      CloudEventBuilder.v1()
        .withId(J.objectId())
        .withTime(OffsetDateTime.now())
        .withSource(URI("client"))
        .withType(type)
        .build()
    }
  println(payload.readValueAndRelease<String>())
}
