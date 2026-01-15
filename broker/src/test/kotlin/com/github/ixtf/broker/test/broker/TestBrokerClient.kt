package com.github.ixtf.broker.test.broker

import cn.hutool.log.Log
import com.github.ixtf.broker.BrokerClient
import com.github.ixtf.broker.BrokerClientOptions
import com.github.ixtf.broker.BrokerRouteOptions
import com.github.ixtf.broker.readValueAndRelease
import com.github.ixtf.core.J
import io.cloudevents.core.builder.CloudEventBuilder
import io.vertx.core.Vertx
import io.vertx.kotlin.core.vertxOptionsOf
import io.vertx.kotlin.coroutines.vertxFuture
import java.net.URI
import java.time.OffsetDateTime

private val log = Log.get()
private val vertx = Vertx.vertx(vertxOptionsOf(preferNativeTransport = true))
private val brokerClient = BrokerClient.create(vertx, BrokerClientOptions())
private val brokerRoute = brokerClient.route(BrokerRouteOptions("test"))

fun main() {
  test("test")
  test("other1")
  test("other2")
  test("other3")
}

private fun test(type: String) =
  vertxFuture(vertx) { requestResponse(type) }.onSuccess { log.info(it) }

private suspend fun requestResponse(type: String): String =
  brokerRoute
    .requestResponse {
      CloudEventBuilder.v1()
        .withId(J.objectId())
        .withTime(OffsetDateTime.now())
        .withSource(URI("client"))
        .withType(type)
        .build()
    }
    .readValueAndRelease()
