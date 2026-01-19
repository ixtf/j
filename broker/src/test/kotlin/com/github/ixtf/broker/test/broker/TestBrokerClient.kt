package com.github.ixtf.broker.test.broker

import cn.hutool.log.Log
import cn.hutool.system.SystemUtil
import com.github.ixtf.broker.BrokerClient
import com.github.ixtf.broker.BrokerClient.Companion.brokerToken
import com.github.ixtf.broker.IXTF_BROKER_TARGET
import com.github.ixtf.broker.kit.readValueAndRelease
import com.github.ixtf.broker.verticle.RSocketMonitorVerticle
import com.github.ixtf.core.J
import io.cloudevents.core.builder.CloudEventBuilder
import io.vertx.core.Vertx
import io.vertx.kotlin.core.vertxOptionsOf
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.vertxFuture
import java.net.URI
import java.time.OffsetDateTime
import java.util.concurrent.atomic.AtomicInteger

private val log = Log.get()
private val vertx = Vertx.vertx(vertxOptionsOf(preferNativeTransport = true))
private val brokerRoute by lazy {
  val token = vertx.brokerToken()
  BrokerClient.create(vertx, token).route("test")
}
private val count = AtomicInteger()

suspend fun main() {
  val osInfo = SystemUtil.getOsInfo()
  if (osInfo.isMac) {
    IXTF_BROKER_TARGET = "192.168.3.31:39998"
  }
  vertx.setPeriodic(0, 5000) { _ ->
    test("test  [${count.incrementAndGet()}]")
    test("other [${count.incrementAndGet()}]")
  }
  vertx.deployVerticle(RSocketMonitorVerticle).coAwait()
}

private fun test(type: String) =
  vertxFuture(vertx) { requestResponse(type) }
    .onSuccess { log.info(it) }
    .onFailure { log.error(it) }

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
