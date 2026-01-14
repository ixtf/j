package com.github.ixtf.broker.verticle

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.ixtf.broker.BrokerKit.toBuffer
import com.github.ixtf.broker.Env.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.dto.SetupDTO
import com.github.ixtf.broker.internal.InternalKit.defaultAuthProvider
import com.github.ixtf.broker.internal.domain.RSocketServer
import com.github.ixtf.core.J
import com.github.ixtf.core.MAPPER
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.rsocket.Closeable
import io.rsocket.ConnectionSetupPayload
import io.rsocket.RSocket
import io.rsocket.SocketAcceptor
import io.rsocket.core.Resume
import io.rsocket.frame.decoder.PayloadDecoder
import io.vertx.ext.auth.authentication.TokenCredentials
import io.vertx.kotlin.coroutines.coAwait
import java.time.Duration
import kotlin.getValue
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

abstract class RSocketServerVerticle(
  id: String = J.objectId(),
  name: String = "RSocket",
  target: String = IXTF_API_BROKER_TARGET,
) : BaseCoroutineVerticle(), SocketAcceptor, RSocket {
  protected open val jwtAuth by lazy { vertx.defaultAuthProvider() }
  private val server by lazy {
    val (host, port) = target.split(":")
    RSocketServer(id = id, name = name, host = host, port = port.toInt())
  }
  private lateinit var closeable: Closeable

  override suspend fun start() {
    super.start()
    closeable =
      io.rsocket.core.RSocketServer.create(this)
        .payloadDecoder(PayloadDecoder.ZERO_COPY)
        .resume(Resume().sessionDuration(Duration.ofMinutes(5)))
        .bind(server.transport())
        .awaitSingle()
  }

  override suspend fun stop() {
    closeable.dispose()
    super.stop()
  }

  override fun accept(setup: ConnectionSetupPayload, sendingSocket: RSocket): Mono<RSocket> = mono {
    val dto = MAPPER.readValue<SetupDTO>(setup.toBuffer().bytes)
    jwtAuth?.authenticate(TokenCredentials(dto.token))?.coAwait()
    this@RSocketServerVerticle
  }
}
