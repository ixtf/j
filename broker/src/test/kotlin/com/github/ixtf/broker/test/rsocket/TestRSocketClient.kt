package com.github.ixtf.broker.test.rsocket

import cn.hutool.log.Log
import com.github.ixtf.broker.BrokerClient.Companion.brokerToken
import com.github.ixtf.broker.IXTF_BROKER_TARGET
import com.github.ixtf.broker.internal.kit.ClientTarget
import com.github.ixtf.broker.internal.kit.InternalKit.buildConnectionSetupPayload
import com.github.ixtf.broker.kit.toPayload
import com.github.ixtf.broker.verticle.RSocketMonitorVerticle
import com.github.ixtf.core.J
import io.cloudevents.core.builder.CloudEventBuilder
import io.rsocket.core.RSocketClient
import io.rsocket.core.RSocketConnector
import io.rsocket.frame.decoder.PayloadDecoder
import io.vertx.core.Vertx
import io.vertx.kotlin.core.vertxOptionsOf
import io.vertx.kotlin.coroutines.coAwait
import java.net.URI
import java.time.OffsetDateTime
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Flux

private val log = Log.get()
// private val vertx = Vertx.vertx()
private val vertx = Vertx.vertx(vertxOptionsOf(preferNativeTransport = true))
private val rSocketClient by lazy {
  RSocketClient.from(
    RSocketConnector.create()
      .payloadDecoder(PayloadDecoder.ZERO_COPY)
      .setupPayload(buildConnectionSetupPayload(vertx.brokerToken()))
      .connect(ClientTarget(IXTF_BROKER_TARGET).transport())
  )
}
private val count = AtomicInteger()

suspend fun main() {
  vertx.deployVerticle(RSocketMonitorVerticle).coAwait()
  val start = System.nanoTime()

  vertx.setPeriodic(0, 5000) { _ ->
    val now = System.nanoTime()
    val durationSec = (now - start) / 1_000_000_000.0
    val qps = (count.get() / durationSec).toInt()
    log.info("--- Monitor --- [QPS: $qps]")
  }

  Flux.range(0, 1_000_000)
    .flatMap(
      {
        rSocketClient
          .requestResponse(
            mono {
              CloudEventBuilder.v1()
                .withId(J.objectId())
                .withTime(OffsetDateTime.now())
                .withSource(URI("client"))
                .withType("ping  [${count.incrementAndGet()}]")
                .build()
                .toPayload()
            }
          )
          .doOnSuccess {
            it?.release()
            count.incrementAndGet()
          }
      },
      1024,
    )
    .doOnComplete {
      val end = System.nanoTime()
      val totalMs = (end - start) / 1_000_000
      println("完成 $count 次请求，总耗时: ${totalMs}ms")
      println("平均 QPS: ${count.get() * 1000 / totalMs}")
    }
    .awaitLast()
}
