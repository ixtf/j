package com.github.ixtf.broker.internal

import com.github.ixtf.broker.BrokerClient
import com.github.ixtf.broker.BrokerClientOptions
import com.github.ixtf.broker.BrokerRouteOptions
import com.github.ixtf.broker.dto.SetupDTO
import com.github.ixtf.broker.internal.InternalKit.buildConnectionSetupPayload
import com.github.ixtf.broker.internal.InternalKit.tcpClientTransport
import io.rsocket.Payload
import io.rsocket.core.RSocketClient
import io.rsocket.core.RSocketConnector
import io.rsocket.frame.decoder.PayloadDecoder
import io.vertx.core.Vertx
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

internal class DefaultBrokerClient(val vertx: Vertx, val options: BrokerClientOptions) :
  BrokerClient {
  override val target by options::target
  private val delegate: RSocketClient by lazy {
    RSocketClient.from(
      RSocketConnector.create()
        .payloadDecoder(PayloadDecoder.ZERO_COPY)
        .setupPayload(
          buildConnectionSetupPayload {
            SetupDTO(
              host = options.host,
              service = options.service?.takeIf { it.isNotBlank() },
              instance = options.instance?.takeIf { it.isNotBlank() },
              tags = options.tags?.takeIf { it.isNotEmpty() },
              token = options.token?.takeIf { it.isNotBlank() },
            )
          }
        )
        .reconnect(InternalKit.defaultRetry(this@DefaultBrokerClient))
        .connect(tcpClientTransport(target))
    )
  }

  override fun route(route: BrokerRouteOptions) = DefaultBrokerRoute(this, route)

  override fun fireAndForget(payloadMono: Mono<Payload>): Mono<Void> =
    delegate.fireAndForget(payloadMono)

  override fun requestResponse(payloadMono: Mono<Payload>): Mono<Payload> =
    delegate.requestResponse(payloadMono)

  override fun requestStream(payloadMono: Mono<Payload>): Flux<Payload> =
    delegate.requestStream(payloadMono)

  override fun requestChannel(payloads: Publisher<Payload>): Flux<Payload> =
    delegate.requestChannel(payloads)

  override fun metadataPush(payloadMono: Mono<Payload>): Mono<Void> =
    delegate.metadataPush(payloadMono)

  override fun dispose() = delegate.dispose()

  override fun isDisposed(): Boolean = delegate.isDisposed
}
