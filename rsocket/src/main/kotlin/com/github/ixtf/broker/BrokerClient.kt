package com.github.ixtf.broker

import cn.hutool.log.Log
import com.github.ixtf.broker.RSocketStatus.*
import com.github.ixtf.broker.internal.doAfterTerminate
import io.rsocket.ConnectionSetupPayload
import io.rsocket.Payload
import io.rsocket.RSocket
import io.rsocket.SocketAcceptor
import io.rsocket.core.RSocketClient
import kotlin.properties.Delegates
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono

enum class RSocketStatus {
  INIT,
  UP,
  DOWN,
  DISPOSE,
}

class BrokerClient(val service: String, val principal: String) : RSocketClient, SocketAcceptor {
  private val log = Log.get("$service[$principal]")
  private lateinit var serviceRSocket: RSocket
  private lateinit var delegate: RSocketClient

  // ⚠️别调用
  @Synchronized
  internal fun initConnect(serviceRSocket: RSocket, delegateRSocket: Mono<RSocket>) {
    this.serviceRSocket = serviceRSocket
    if (::delegate.isInitialized) delegate.dispose()
    delegate = RSocketClient.from(delegateRSocket)
    delegate.connect()
  }

  fun reConnect() {
    //    fireAndForget(brokerRequest { buildReConnect() })
    //      .subscribeOn(Schedulers.boundedElastic())
    //      .subscribe()
  }

  var status: RSocketStatus by
    Delegates.observable(RSocketStatus.INIT) { prop, old, new ->
      if (old == new) return@observable
      log.warn("$old -> $new")
      if (delegate.isDisposed) {
        status = DISPOSE
        return@observable
      }
      when (new) {
        INIT,
        UP,
        DISPOSE -> Unit
        DOWN -> reConnect()
      }
    }
    private set

  override fun accept(setup: ConnectionSetupPayload, sendingSocket: RSocket): Mono<RSocket> {
    sendingSocket.doAfterTerminate { status = if (delegate.isDisposed) DISPOSE else DOWN }
    return Mono.just(serviceRSocket).doOnSuccess { status = UP }
  }

  override fun source() = delegate.source()

  override fun fireAndForget(payloadMono: Mono<Payload>) = delegate.fireAndForget(payloadMono)

  override fun requestResponse(payloadMono: Mono<Payload>) = delegate.requestResponse(payloadMono)

  override fun requestStream(payloadMono: Mono<Payload>) = delegate.requestStream(payloadMono)

  override fun requestChannel(payloads: Publisher<Payload>) = delegate.requestChannel(payloads)

  override fun metadataPush(payloadMono: Mono<Payload>) = delegate.metadataPush(payloadMono)

  override fun dispose() = delegate.dispose()

  override fun connect() = delegate.connect()

  override fun onClose() = delegate.onClose()

  override fun isDisposed() = delegate.isDisposed
}
