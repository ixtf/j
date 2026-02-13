package com.github.ixtf.broker.test.broker

import cn.hutool.log.Log
import cn.hutool.system.SystemUtil
import com.github.ixtf.broker.BrokerClient
import com.github.ixtf.broker.BrokerClient.Companion.brokerToken
import com.github.ixtf.broker.kit.readValueAndRelease
import com.github.ixtf.broker.verticle.RSocketMonitorVerticle
import com.github.ixtf.core.J
import io.cloudevents.core.builder.CloudEventBuilder
import io.netty.handler.ssl.OpenSsl
import io.netty.handler.ssl.SslProvider
import io.vertx.core.Vertx
import io.vertx.kotlin.core.vertxOptionsOf
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.vertxFuture
import java.net.URI
import java.time.OffsetDateTime
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Flux

private val log = Log.get()
private val vertx = Vertx.vertx(vertxOptionsOf(preferNativeTransport = true))
private val brokerClient by lazy { BrokerClient.create(vertx, vertx.brokerToken()) }
private val brokerRoute by lazy { brokerClient.route("test") }
private val count = AtomicInteger()

suspend fun main() {
  println("Is Native Transport Enabled: ${vertx.isNativeTransportEnabled}")
  println("OpenSSL Available: ${OpenSsl.isAvailable()}")
  println("ALPN Supported: ${SslProvider.isAlpnSupported(SslProvider.OPENSSL)}")
  OpenSsl.unavailabilityCause()?.printStackTrace()

  val osInfo = SystemUtil.getOsInfo()
  if (osInfo.isMac) {
    // IXTF_BROKER_TARGET = "192.168.3.31:39998"
  }
  vertx.deployVerticle(RSocketMonitorVerticle).coAwait()
  val start = System.nanoTime()

  vertx.setPeriodic(0, 5000) { _ ->
    // test("test  [${count.incrementAndGet()}]")
    // test("other [${count.incrementAndGet()}]")

    val now = System.nanoTime()
    val durationSec = (now - start) / 1_000_000_000.0
    val qps = (count.get() / durationSec).toInt()
    log.info("--- Monitor --- [QPS: $qps]")
  }

  Flux.range(0, 1_000_000)
    .flatMap(
      { mono { requestResponse("ping  [${count.incrementAndGet()}]") } },
      1024,
    ) // 保持 256 个并发请求
    .doOnComplete {
      val end = System.nanoTime()
      val totalMs = (end - start) / 1_000_000
      println("完成 $count 次请求，总耗时: ${totalMs}ms")
      println("平均 QPS: ${count.get() * 1000 / totalMs}")
    }
    .awaitLast()
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
