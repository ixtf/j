package com.github.ixtf.broker.internal.application

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.ixtf.broker.BrokerKit.toBuffer
import com.github.ixtf.broker.dto.SetupDTO
import com.github.ixtf.broker.internal.InternalKit.doAfterTerminate
import com.github.ixtf.broker.internal.domain.BrokerServer
import com.github.ixtf.broker.internal.domain.event.BrokerServerEvent
import com.github.ixtf.core.MAPPER
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.rsocket.Closeable
import io.rsocket.ConnectionSetupPayload
import io.rsocket.RSocket
import io.rsocket.SocketAcceptor
import io.rsocket.core.RSocketServer
import io.rsocket.core.Resume
import io.rsocket.frame.decoder.PayloadDecoder
import io.vertx.ext.auth.authentication.AuthenticationProvider
import io.vertx.ext.auth.authentication.TokenCredentials
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.receiveChannelHandler
import java.time.Duration
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

internal class BrokerServerEntity(
  private var server: BrokerServer,
  private val serverRSocket: RSocket,
  private val authProvider: AuthenticationProvider? = null,
) : BaseCoroutineVerticle(), SocketAcceptor {
  private val channel by lazy { vertx.receiveChannelHandler<BrokerServerEvent>() }
  private lateinit var closeable: Closeable

  internal fun currentState(): BrokerServer = server

  override suspend fun start() {
    super.start()
    closeable =
      RSocketServer.create(this)
        .payloadDecoder(PayloadDecoder.ZERO_COPY)
        .resume(Resume().sessionDuration(Duration.ofMinutes(5)))
        .bind(server.transport())
        .awaitSingle()

    launch {
      channel.consumeEach { event ->
        try {
          server =
            when (event) {
              is BrokerServerEvent.Connected -> server.onEvent(event)
              is BrokerServerEvent.DisConnected -> server.onEvent(event)
            }
        } catch (_: CancellationException) {
          // ignore
        } catch (t: Throwable) {
          log.error(t, "state: {}", server)
        }
      }
    }
  }

  override suspend fun stop() {
    closeable.dispose()
    super.stop()
  }

  override fun accept(setup: ConnectionSetupPayload, sendingSocket: RSocket): Mono<RSocket> = mono {
    val dto = MAPPER.readValue<SetupDTO>(setup.toBuffer().bytes)
    if (authProvider != null) {
      require(dto.token.isNullOrBlank().not())
      authProvider.authenticate(TokenCredentials(dto.token)).coAwait()
      if (dto.service.isNullOrBlank().not()) {
        require(dto.principal.isNullOrBlank().not())
        channel.handle(
          BrokerServerEvent.Connected(
            rSocket = sendingSocket,
            instance = dto.principal,
            service = dto.service,
            host = dto.host,
            tags = dto.tags,
          )
        )
        sendingSocket.doAfterTerminate {
          channel.handle(
            BrokerServerEvent.DisConnected(service = dto.service, instance = dto.principal)
          )
        }
      }
    }
    serverRSocket
  }
}
