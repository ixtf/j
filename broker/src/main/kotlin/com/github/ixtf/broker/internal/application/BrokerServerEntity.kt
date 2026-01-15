package com.github.ixtf.broker.internal.application

import com.github.ixtf.broker.dto.SetupDTO
import com.github.ixtf.broker.internal.InternalKit.doAfterTerminate
import com.github.ixtf.broker.internal.domain.BrokerServer
import com.github.ixtf.broker.internal.domain.event.BrokerServerEvent
import com.github.ixtf.broker.readValue
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.netty.util.ReferenceCountUtil
import io.rsocket.Closeable
import io.rsocket.ConnectionSetupPayload
import io.rsocket.Payload
import io.rsocket.RSocket
import io.rsocket.SocketAcceptor
import io.rsocket.core.RSocketServer
import io.rsocket.frame.decoder.PayloadDecoder
import io.rsocket.loadbalance.LoadbalanceStrategy
import io.vertx.ext.auth.authentication.AuthenticationProvider
import io.vertx.ext.auth.authentication.TokenCredentials
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.receiveChannelHandler
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

internal class BrokerServerEntity(
  private var server: BrokerServer,
  private val authProvider: AuthenticationProvider?,
  private val lbStrategy: LoadbalanceStrategy,
  private val brokerRSocket: RSocket,
) : BaseCoroutineVerticle(), SocketAcceptor, RSocket {
  private val channel by lazy { vertx.receiveChannelHandler<BrokerServerEvent>() }
  private lateinit var closeable: Closeable
  internal val entityId by server::id

  override suspend fun start() {
    super.start()
    closeable =
      RSocketServer.create(this)
        .payloadDecoder(PayloadDecoder.ZERO_COPY)
        // .resume(Resume().sessionDuration(Duration.ofMinutes(5)))
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

  override fun accept(setup: ConnectionSetupPayload, sendingSocket: RSocket): Mono<RSocket> =
    mono {
        val dto = setup.readValue<SetupDTO>()
        authProvider?.also { _ ->
          require(dto.token.isNullOrBlank().not())
          authProvider.authenticate(TokenCredentials(dto.token)).coAwait()
          if (dto.service.isNullOrBlank().not()) {
            require(dto.instance.isNullOrBlank().not())
            channel.handle(
              BrokerServerEvent.Connected(
                rSocket = sendingSocket,
                instance = dto.instance,
                service = dto.service,
                host = dto.host,
                tags = dto.tags,
              )
            )
            sendingSocket.doAfterTerminate {
              channel.handle(
                BrokerServerEvent.DisConnected(service = dto.service, instance = dto.instance)
              )
            }
          }
        }
        log.warn("setup: {}", dto)
        this@BrokerServerEntity as RSocket
      }
      .doOnError { log.error(it) }

  override fun metadataPush(payload: Payload): Mono<Void> =
    mono { BrokerContext(payload).pickRSocket(server, lbStrategy, brokerRSocket) }
      .doOnError { ReferenceCountUtil.safeRelease(payload) }
      .flatMap { it.metadataPush(payload) }

  override fun fireAndForget(payload: Payload): Mono<Void> =
    mono { BrokerContext(payload).pickRSocket(server, lbStrategy, brokerRSocket) }
      .doOnError { ReferenceCountUtil.safeRelease(payload) }
      .flatMap { it.fireAndForget(payload) }

  override fun requestResponse(payload: Payload): Mono<Payload> =
    mono { BrokerContext(payload).pickRSocket(server, lbStrategy, brokerRSocket) }
      .doOnError { ReferenceCountUtil.safeRelease(payload) }
      .flatMap { it.requestResponse(payload) }

  override fun requestStream(payload: Payload): Flux<Payload> =
    mono { BrokerContext(payload).pickRSocket(server, lbStrategy, brokerRSocket) }
      .doOnError { ReferenceCountUtil.safeRelease(payload) }
      .flatMapMany { it.requestStream(payload) }

  override fun requestChannel(payloads: Publisher<Payload>): Flux<Payload> =
    Flux.from(payloads).switchOnFirst { signal, flux ->
      val payload = signal.get()
      mono { BrokerContext(requireNotNull(payload)).pickRSocket(server, lbStrategy, brokerRSocket) }
        .doOnError { ReferenceCountUtil.safeRelease(payload) }
        .flatMapMany { it.requestChannel(payloads) }
    }
}
