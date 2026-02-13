package com.github.ixtf.broker.test.broker

import cn.hutool.system.SystemUtil
import com.github.ixtf.broker.BrokerClient.Companion.brokerToken
import com.github.ixtf.broker.IXTF_BROKER_TARGET
import com.github.ixtf.broker.SetupInfo
import com.github.ixtf.broker.kit.readValueAndRelease
import com.github.ixtf.broker.kit.readValueOrNull
import com.github.ixtf.broker.kit.toPayload
import com.github.ixtf.broker.verticle.BrokerServiceVerticle
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
// const val SERVICE_COUNT = 100
const val SERVICE_COUNT = 1

suspend fun main() {
  println("Is Native Transport Enabled: ${vertx.isNativeTransportEnabled}")
  println("OpenSSL Available: ${OpenSsl.isAvailable()}")
  println("ALPN Supported: ${SslProvider.isAlpnSupported(SslProvider.OPENSSL)}")
  OpenSsl.unavailabilityCause()?.printStackTrace()

  val osInfo = SystemUtil.getOsInfo()
  if (osInfo.isMac) {
    IXTF_BROKER_TARGET = "192.168.3.31:39998"
  }
  vertx.deployVerticle(RSocketMonitorVerticle).coAwait()

  List(SERVICE_COUNT) { vertx.deployVerticle(TestBrokerService(it)).coAwait() }
}

private class TestBrokerService(idx: Int = 0, private val service: String = "test $idx") :
  BrokerServiceVerticle(vertx.brokerToken(SetupInfo(service = service))) {
  override fun requestResponse(payload: Payload): Mono<Payload> = mono {
    val ce = payload.readValueAndRelease<CloudEvent>()
    val data = ce.readValueOrNull() ?: ce.type
    val response = "$service: $data"
    //    log.info(response)
    //    when {
    //      ce.type.startsWith("test") -> delay(5.seconds)
    //      else -> delay(RandomUtil.randomLong(500, 3000))
    //    }
    // delay(RandomUtil.randomLong(50, 200))
    // delay(RandomUtil.randomLong(5, 20))
    Buffer.buffer(response).toPayload()
  }
}
