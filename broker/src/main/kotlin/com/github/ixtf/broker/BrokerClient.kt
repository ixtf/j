package com.github.ixtf.broker

import com.github.ixtf.broker.internal.DefaultBrokerClient
import io.rsocket.Payload
import io.vertx.core.Vertx
import org.reactivestreams.Publisher
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface BrokerClient : Disposable {
  val target: String

  fun route(route: RouteOptions): BrokerRoute

  fun fireAndForget(payloadMono: Mono<Payload>): Mono<Void>

  fun requestResponse(payloadMono: Mono<Payload>): Mono<Payload>

  fun requestStream(payloadMono: Mono<Payload>): Flux<Payload>

  fun requestChannel(payloads: Publisher<Payload>): Flux<Payload>

  fun metadataPush(payloadMono: Mono<Payload>): Mono<Void>

  companion object {
    fun create(vertx: Vertx, options: BrokerClientOptions = BrokerClientOptions()): BrokerClient =
      DefaultBrokerClient(vertx, options)
  }
}
