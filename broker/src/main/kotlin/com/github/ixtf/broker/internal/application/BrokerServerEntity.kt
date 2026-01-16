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
  private val authProvider: AuthenticationProvider,
  private val lbStrategy: LoadbalanceStrategy,
  private val brokerRSocket: RSocket,
) : BaseCoroutineVerticle(), SocketAcceptor, RSocket {
  private val channel by lazy { vertx.receiveChannelHandler<BrokerServerEvent>() }
  private lateinit var closeable: Closeable

  override suspend fun start() {
    super.start()
    closeable =
      RSocketServer.create(this)
        .payloadDecoder(PayloadDecoder.ZERO_COPY)
        .resume(InternalKit.defaultResume(this))
        .bind(InternalKit.serverTransport(server.target))
        .awaitSingle()

    launch {
      channel.consumeEach { event ->
        log.warn("{}", event)
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
    val credentials = TokenCredentials(setup.readValue<String>())
    val user = authProvider.authenticate(credentials).coAwait()
    val dto = user.principal().mapTo(SetupDTO::class.java)
    accept(dto, sendingSocket)
    this@BrokerServerEntity
  }

  internal fun accept(setup: SetupDTO, sendingSocket: RSocket) {
    if (setup.service.isNullOrBlank()) {
      log.debug("client: {}", setup)
    } else {
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
  }

  override fun metadataPush(payload: Payload): Mono<Void> =
    mono { BrokerContext(server, payload).pickRSocket(lbStrategy, brokerRSocket) }
      .doOnError { ReferenceCountUtil.safeRelease(payload) }
      .flatMap { it.metadataPush(payload) }

  override fun fireAndForget(payload: Payload): Mono<Void> =
    mono { BrokerContext(server, payload).pickRSocket(lbStrategy, brokerRSocket) }
      .doOnError { ReferenceCountUtil.safeRelease(payload) }
      .flatMap { it.fireAndForget(payload) }

  override fun requestResponse(payload: Payload): Mono<Payload> =
    mono { BrokerContext(server, payload).pickRSocket(lbStrategy, brokerRSocket) }
      .doOnError { ReferenceCountUtil.safeRelease(payload) }
      .flatMap { it.requestResponse(payload) }

  override fun requestStream(payload: Payload): Flux<Payload> =
    mono { BrokerContext(server, payload).pickRSocket(lbStrategy, brokerRSocket) }
      .doOnError { ReferenceCountUtil.safeRelease(payload) }
      .flatMapMany { it.requestStream(payload) }

  override fun requestChannel(payloads: Publisher<Payload>): Flux<Payload> =
    Flux.from(payloads).switchOnFirst { signal, flux ->
      val payload = requireNotNull(signal.get())
      mono { BrokerContext(server, payload).pickRSocket(lbStrategy, brokerRSocket) }
        .doOnError { ReferenceCountUtil.safeRelease(payload) }
        .flatMapMany { it.requestChannel(payloads) }
    }
}
