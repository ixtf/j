package com.github.ixtf.broker.test.broker

import cn.hutool.core.util.RandomUtil
import com.github.ixtf.broker.BrokerClient.Companion.brokerToken
import com.github.ixtf.broker.Env.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.SetupInfo
import com.github.ixtf.broker.kit.readValueAndRelease
import com.github.ixtf.broker.kit.readValueOrNull
import com.github.ixtf.broker.kit.toPayload
import com.github.ixtf.broker.verticle.BrokerServiceVerticle
import io.cloudevents.CloudEvent
import io.rsocket.Payload
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.kotlin.core.vertxOptionsOf
import io.vertx.kotlin.coroutines.coAwait
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

private val vertx = Vertx.vertx(vertxOptionsOf(preferNativeTransport = true))
private val token = vertx.brokerToken(SetupInfo(service = "test", instance = "test"))

suspend fun main() {
  IXTF_API_BROKER_TARGET = "192.168.3.31:39998"
  vertx.deployVerticle(TestBrokerService()).coAwait()

  println("isNativeTransportEnabled: ${vertx.isNativeTransportEnabled}")
}

private class TestBrokerService : BrokerServiceVerticle(token) {
  override fun requestResponse(payload: Payload): Mono<Payload> = mono {
    val ce = payload.readValueAndRelease<CloudEvent>()
    log.info("requestResponse: ${ce.type}")
    val data =
      when {
        ce.type.startsWith("test") -> {
          delay(5.seconds)
          ce.readValueOrNull() ?: "requestResponse: ${ce.type}"
        }
        else -> {
          delay(RandomUtil.randomLong(500, 3000))
          ce.readValueOrNull() ?: "requestResponse: ${ce.type}"
        }
      }
    Buffer.buffer(data).toPayload()
  }
}
