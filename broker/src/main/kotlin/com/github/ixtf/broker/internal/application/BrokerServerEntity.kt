package com.github.ixtf.broker.internal.application

import com.github.ixtf.broker.SetupInfo
import com.github.ixtf.broker.assember.BrokerServerInfo
import com.github.ixtf.broker.internal.domain.BrokerServer
import com.github.ixtf.broker.internal.domain.event.BrokerServerEvent
import com.github.ixtf.broker.internal.kit.doOnClose
import com.github.ixtf.core.J
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.netty.util.ReferenceCountUtil
import io.rsocket.Payload
import io.rsocket.RSocket
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.receiveChannelHandler
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.mono
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

internal class BrokerServerEntity(
  private var server: BrokerServer,
  private val brokerRSocket: RSocket,
) : BaseCoroutineVerticle(), RSocket {
  private val channel by lazy { vertx.receiveChannelHandler<BrokerServerEvent>() }

  override suspend fun start() {
    super.start()
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

        if (log.isDebugEnabled) {
          val info = JsonObject.mapFrom(BrokerServerInfo(server))
          log.debug(info.encodePrettily())
        }
      }
    }
  }

  internal fun accept(setup: SetupInfo, sendingSocket: RSocket): RSocket = apply {
    if (setup.service.isNullOrBlank()) {
      log.debug("client: {}", setup)
    } else {
      channel.handle(
        BrokerServerEvent.Connected(
          service = setup.service,
          rSocket = sendingSocket,
          instance = if (setup.instance.isNullOrBlank()) J.objectId() else setup.instance,
          host = setup.host,
          tags = setup.tags,
        )
      )
      sendingSocket.doOnClose(log) {
        channel.handle(
          BrokerServerEvent.DisConnected(service = setup.service, rSocket = sendingSocket)
        )
      }
    }
  }

  internal fun rSocketMono(payload: Payload): Mono<RSocket> =
    mono { BrokerMetadata(server, payload).pickRSocket(brokerRSocket) }
      .doOnError {
        ReferenceCountUtil.safeRelease(payload)
        log.error(it)
      }

  override fun metadataPush(payload: Payload): Mono<Void> =
    rSocketMono(payload).flatMap { it.metadataPush(payload) }

  override fun fireAndForget(payload: Payload): Mono<Void> =
    rSocketMono(payload).flatMap { it.fireAndForget(payload) }

  override fun requestResponse(payload: Payload): Mono<Payload> =
    rSocketMono(payload).flatMap { it.requestResponse(payload) }

  override fun requestStream(payload: Payload): Flux<Payload> =
    rSocketMono(payload).flatMapMany { it.requestStream(payload) }

  override fun requestChannel(payloads: Publisher<Payload>): Flux<Payload> =
    Flux.from(payloads).switchOnFirst { signal, _ ->
      rSocketMono(requireNotNull(signal.get())).flatMapMany { it.requestChannel(payloads) }
    }
}
