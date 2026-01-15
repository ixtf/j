package com.github.ixtf.broker.internal.application

import com.github.ixtf.broker.dto.SetupDTO
import com.github.ixtf.broker.internal.InternalKit
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

  internal fun accept(setup: SetupDTO, sendingSocket: RSocket) {
    if (setup.service.isNullOrBlank().not()) {
      require(setup.instance.isNullOrBlank().not())
      channel.handle(
        BrokerServerEvent.Connected(
          rSocket = sendingSocket,
          instance = setup.instance,
          service = setup.service,
          host = setup.host,
          tags = setup.tags,
        )
      )
      sendingSocket.doAfterTerminate {
        channel.handle(
          BrokerServerEvent.DisConnected(service = setup.service, instance = setup.instance)
        )
      }
    }
    log.debug("{}", setup)
  }

  override suspend fun start() {
    super.start()
    closeable =
      RSocketServer.create(this)
        .payloadDecoder(PayloadDecoder.ZERO_COPY)
        .resume(InternalKit.defaultResume(this))
        .bind(server.transport())
        .awaitSingle()

    launch {
      channel.consumeEach { event ->
        runCatching {
            when (event) {
              is BrokerServerEvent.Connected -> server.onEvent(event)
              is BrokerServerEvent.DisConnected -> server.onEvent(event)
            }
          }
          .onSuccess { server = it }
          .onFailure { log.error(it, "state: {}", server) }
      }
    }
  }

  override suspend fun stop() {
    closeable.dispose()
    super.stop()
  }

  override fun accept(setup: ConnectionSetupPayload, sendingSocket: RSocket): Mono<RSocket> = mono {
    val dto = setup.readValue<SetupDTO>()
    authProvider?.apply {
      require(dto.token.isNullOrBlank().not())
      authenticate(TokenCredentials(dto.token)).coAwait()
    }
    accept(dto, sendingSocket)
    this@BrokerServerEntity
  }

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
