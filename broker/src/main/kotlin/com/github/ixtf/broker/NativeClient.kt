package com.github.ixtf.broker

import io.rsocket.Payload
import org.reactivestreams.Publisher
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface NativeClient : Disposable {
  fun fireAndForget(payloadMono: Mono<Payload>): Mono<Void>

  fun requestResponse(payloadMono: Mono<Payload>): Mono<Payload>

  fun requestStream(payloadMono: Mono<Payload>): Flux<Payload>

  fun requestChannel(payloads: Publisher<Payload>): Flux<Payload>

  fun metadataPush(payloadMono: Mono<Payload>): Mono<Void>
}
