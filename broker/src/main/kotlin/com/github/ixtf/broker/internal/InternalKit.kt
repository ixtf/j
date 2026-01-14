package com.github.ixtf.broker.internal

import com.github.ixtf.broker.Env.IXTF_API_BROKER_AUTH
import io.rsocket.RSocket
import io.rsocket.transport.ClientTransport
import io.rsocket.transport.ServerTransport
import io.rsocket.transport.netty.client.TcpClientTransport
import io.rsocket.transport.netty.server.TcpServerTransport
import io.vertx.core.Vertx
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.kotlin.ext.auth.jwt.jwtAuthOptionsOf
import io.vertx.kotlin.ext.auth.pubSecKeyOptionsOf
import java.time.Duration
import reactor.core.scheduler.Schedulers
import reactor.util.retry.Retry
import reactor.util.retry.RetryBackoffSpec

internal object InternalKit {
  internal fun Vertx.defaultAuthProvider(buffer: String? = IXTF_API_BROKER_AUTH): JWTAuth? {
    if (buffer.isNullOrBlank()) return null
    val key = pubSecKeyOptionsOf(algorithm = "HS256").setBuffer(buffer)
    val config = jwtAuthOptionsOf(pubSecKeys = listOf(key))
    return JWTAuth.create(this, config)
  }

  internal fun RSocket.doAfterTerminate(block: () -> Unit) {
    onClose().doAfterTerminate(block).subscribeOn(Schedulers.boundedElastic()).subscribe()
  }

  internal fun tcpServerTransport(target: String): ServerTransport<*> {
    val (bindAddress, port) = target.split(":")
    return TcpServerTransport.create(bindAddress, port.toInt())
  }

  internal fun tcpClientTransport(target: String): ClientTransport {
    val (bindAddress, port) = target.split(":")
    return TcpClientTransport.create(bindAddress, port.toInt())
  }

  internal fun defaultRetry(): RetryBackoffSpec =
    Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
      .maxBackoff(Duration.ofSeconds(3))
      .jitter(0.5)
}
