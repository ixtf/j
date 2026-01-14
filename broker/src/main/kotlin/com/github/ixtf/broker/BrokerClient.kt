package com.github.ixtf.broker

import cn.hutool.log.Log
import com.github.ixtf.broker.Env.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.RSocketStatus.*
import com.github.ixtf.broker.internal.ConnectionSetupPayloadBuilder.Companion.buildConnectionSetupPayload
import com.github.ixtf.broker.internal.InternalKit
import com.github.ixtf.broker.internal.InternalKit.doAfterTerminate
import com.github.ixtf.broker.internal.InternalKit.tcpClientTransport
import com.github.ixtf.core.J
import io.rsocket.ConnectionSetupPayload
import io.rsocket.Payload
import io.rsocket.RSocket
import io.rsocket.SocketAcceptor
import io.rsocket.core.RSocketClient
import io.rsocket.core.RSocketConnector
import io.rsocket.core.Resume
import io.rsocket.frame.decoder.PayloadDecoder
import io.rsocket.util.DefaultPayload
import java.time.Duration
import kotlin.properties.Delegates
import kotlinx.coroutines.reactor.mono
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono

enum class RSocketStatus {
  INIT,
  UP,
  DOWN,
  DISPOSE,
}

class BrokerClient(val service: String, val principal: String = J.objectId()) :
  RSocketClient, SocketAcceptor {
  private val log = Log.get(javaClass)
  private lateinit var serviceRSocket: RSocket
  private val delegate: RSocketClient by lazy {
    RSocketClient.from(
      RSocketConnector.create()
        .acceptor(this)
        .payloadDecoder(PayloadDecoder.ZERO_COPY)
        .setupPayload(buildConnectionSetupPayload(service, principal) {})
        .reconnect(
          InternalKit.defaultRetry().doBeforeRetry { signal ->
            log.warn("${this@BrokerClient}，尝试第 ${signal.totalRetries() + 1} 次重连...")
          }
        )
        // 1. 开启 Resume 功能
        .resume(
          Resume()
            .sessionDuration(Duration.ofMinutes(5)) // 允许服务端宕机 5 分钟内恢复 Session
            .retry(
              InternalKit.defaultRetry().doBeforeRetry { signal ->
                log.warn("${this@BrokerClient}，尝试第 ${signal.totalRetries() + 1} 次重连...")
              }
            )
        )
        .connect(tcpClientTransport(IXTF_API_BROKER_TARGET))
    )
  }

  override fun toString(): String = "BrokerClient($service[$principal])"

  init {
    serviceRSocket = object : RSocket {}
    delegate.onClose().subscribe { println("delegate onClose") }
    reConnect()
  }

  fun reConnect() {
    delegate.fireAndForget(mono { DefaultPayload.create(DefaultPayload.EMPTY_BUFFER) }).subscribe()
    //    fireAndForget(brokerRequest { buildReConnect() })
    //      .subscribeOn(Schedulers.boundedElastic())
    //      .subscribe()
  }

  var status: RSocketStatus by
    Delegates.observable(INIT) { prop, old, new ->
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
