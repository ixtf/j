package com.github.ixtf.broker.internal

import com.github.ixtf.broker.BrokerClient
import com.github.ixtf.broker.BrokerRouteOptions
import com.github.ixtf.broker.internal.kit.ClientTarget
import com.github.ixtf.broker.internal.kit.InternalKit
import com.github.ixtf.broker.internal.kit.InternalKit.buildConnectionSetupPayload
import io.rsocket.Payload
import io.rsocket.core.RSocketClient
import io.rsocket.core.RSocketConnector
import io.rsocket.frame.decoder.PayloadDecoder
import io.vertx.core.Vertx
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

internal class DefaultBrokerClient(val vertx: Vertx, token: String, target: ClientTarget) :
  BrokerClient {
  private val delegate =
    RSocketClient.from(
      RSocketConnector.create()
        .payloadDecoder(PayloadDecoder.ZERO_COPY)
        .setupPayload(buildConnectionSetupPayload(token))
        .reconnect(InternalKit.defaultRetry(this))
        // .resume(InternalKit.defaultResume(this))
        .connect(target.transport())
    )

  override fun route(route: BrokerRouteOptions) = DefaultBrokerRouteClient(this, route)

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
