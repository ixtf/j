package com.github.ixtf.broker.verticle

import com.github.ixtf.broker.Env.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.dto.BrokerServiceSetupDTO
import com.github.ixtf.broker.internal.InternalKit
import com.github.ixtf.broker.internal.InternalKit.buildConnectionSetupPayload
import com.github.ixtf.broker.internal.InternalKit.defaultAuthProvider
import com.github.ixtf.broker.internal.InternalKit.tcpClientTransport
import com.github.ixtf.core.J
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.rsocket.Payload
import io.rsocket.RSocket
import io.rsocket.SocketAcceptor
import io.rsocket.core.RSocketClient
import io.rsocket.core.RSocketConnector
import io.rsocket.core.Resume
import io.rsocket.frame.decoder.PayloadDecoder
import io.vertx.kotlin.core.json.jsonObjectOf
import java.time.Duration
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

abstract class BrokerServiceVerticle(
  service: String,
  principal: String,
  tags: Set<String>? = null,
  target: String = IXTF_API_BROKER_TARGET,
  host: String = J.localIp(),
) : BaseCoroutineVerticle(), SocketAcceptor, RSocket {
  protected open val jwtAuth by lazy { vertx.defaultAuthProvider() }
  private val rSocketClient: RSocketClient by lazy {
    RSocketClient.from(
      RSocketConnector.create()
        .acceptor(this)
        .payloadDecoder(PayloadDecoder.ZERO_COPY)
        .setupPayload(
          buildConnectionSetupPayload(
            BrokerServiceSetupDTO(
              host = host,
              service = service,
              principal = principal,
              tags = tags,
              token = jwtAuth?.generateToken(jsonObjectOf()),
            )
          )
        )
        .reconnect(
          InternalKit.defaultRetry().doBeforeRetry { signal ->
            log.warn("${this@BrokerServiceVerticle}，第 ${signal.totalRetries() + 1} 次 Reconnect...")
          }
        )
        .resume(
          Resume()
            .sessionDuration(Duration.ofMinutes(5)) // 允许服务端宕机 5 分钟内恢复 Session
            .retry(
              InternalKit.defaultRetry().doBeforeRetry { signal ->
                log.warn("${this@BrokerServiceVerticle}，第 ${signal.totalRetries() + 1} 次 Resume...")
              }
            )
        )
        .connect(tcpClientTransport(target))
    )
  }

  override suspend fun start() {
    super.start()
    rSocketClient.connect()
  }

  override fun metadataPush(payload: Payload): Mono<Void> {
    return super.metadataPush(payload)
  }

  override fun fireAndForget(payload: Payload): Mono<Void> {
    return super.fireAndForget(payload)
  }

  override fun requestResponse(payload: Payload): Mono<Payload> {
    return super.requestResponse(payload)
  }

  override fun requestStream(payload: Payload): Flux<Payload> {
    return super.requestStream(payload)
  }

  override fun requestChannel(payloads: Publisher<Payload>): Flux<Payload> {
    return super.requestChannel(payloads)
  }
}
