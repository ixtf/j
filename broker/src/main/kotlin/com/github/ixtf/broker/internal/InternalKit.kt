package com.github.ixtf.broker.internal

import cn.hutool.core.util.ReflectUtil
import com.github.ixtf.broker.Env.IXTF_API_BROKER_AUTH
import com.github.ixtf.broker.dto.SetupDTO
import com.github.ixtf.broker.toPayload
import io.rsocket.DuplexConnection
import io.rsocket.Payload
import io.rsocket.RSocket
import io.rsocket.core.Resume
import io.rsocket.transport.ClientTransport
import io.rsocket.transport.ServerTransport
import io.rsocket.transport.netty.client.TcpClientTransport
import io.rsocket.transport.netty.server.TcpServerTransport
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.kotlin.ext.auth.jwt.jwtAuthOptionsOf
import io.vertx.kotlin.ext.auth.pubSecKeyOptionsOf
import java.net.SocketAddress
import java.time.Duration
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.util.retry.Retry
import reactor.util.retry.RetryBackoffSpec

internal object InternalKit {
  internal fun Vertx.defaultAuth(buffer: String? = IXTF_API_BROKER_AUTH): JWTAuth? {
    if (buffer.isNullOrBlank()) return null
    val key = pubSecKeyOptionsOf(algorithm = "HS256").setBuffer(buffer)
    val config = jwtAuthOptionsOf(pubSecKeys = listOf(key))
    return JWTAuth.create(this, config)
  }

  internal fun RSocket.remoteAddress(): SocketAddress? =
    runCatching { ReflectUtil.getFieldValue(this, "connection") }
      .map { (it as? DuplexConnection)?.remoteAddress() }
      .getOrDefault(null)

  internal fun RSocket.doAfterTerminate(block: () -> Unit) {
    onClose().doAfterTerminate(block).subscribeOn(Schedulers.boundedElastic()).subscribe()
  }

  internal fun tcpServerTransport(target: String): ServerTransport<*> {
    val (bindAddress, port) = target.split(":")
    return TcpServerTransport.create(bindAddress, port.toInt())
  }

  internal fun buildConnectionSetupPayload(block: suspend () -> SetupDTO): Mono<Payload> = mono {
    JsonObject.mapFrom(block()).toPayload()
  }

  internal fun tcpClientTransport(target: String): ClientTransport {
    val (bindAddress, port) = target.split(":")
    return TcpClientTransport.create(bindAddress, port.toInt())
  }

  internal fun defaultRetry(source: Any): RetryBackoffSpec =
    Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
      .maxBackoff(Duration.ofSeconds(3))
      .jitter(0.5)
      .doBeforeRetry { println("$source，尝试第 ${it.totalRetries() + 1} 次重连...") }

  /**
   * - server：需要Resume
   * - service：不要Resume，否则连接断开会延迟到Resume过期
   * - client：可以Resume
   */
  internal fun defaultResume(source: Any): Resume =
    Resume().sessionDuration(Duration.ofMinutes(5)).retry(defaultRetry(source))
}
