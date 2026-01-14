package com.github.ixtf.broker.internal

import com.github.ixtf.broker.BrokerClient
import com.github.ixtf.broker.BrokerClientOptions
import com.github.ixtf.broker.dto.SetupDTO
import com.github.ixtf.broker.internal.InternalKit.buildConnectionSetupPayload
import com.github.ixtf.broker.internal.InternalKit.tcpClientTransport
import io.cloudevents.CloudEvent
import io.rsocket.Payload
import io.rsocket.core.RSocketClient
import io.rsocket.core.RSocketConnector
import io.rsocket.core.Resume
import io.rsocket.frame.decoder.PayloadDecoder
import io.vertx.core.Vertx
import java.time.Duration
import kotlinx.coroutines.flow.Flow
import reactor.core.publisher.Mono

internal class DefaultBrokerClient(
  private val vertx: Vertx,
  private val options: BrokerClientOptions,
) : BrokerClient {
  override val target by options::target
  private val rSocketClient: RSocketClient by lazy {
    RSocketClient.from(
      RSocketConnector.create()
        .payloadDecoder(PayloadDecoder.ZERO_COPY)
        .setupPayload(
          buildConnectionSetupPayload(
            SetupDTO(
              host = options.host,
              service = options.service?.takeIf { it.isNotBlank() },
              instance = options.instance?.takeIf { it.isNotBlank() },
              tags = options.tags?.takeIf { it.isNotEmpty() },
              token = options.token?.takeIf { it.isNotBlank() },
            )
          )
        )
        .reconnect(
          InternalKit.defaultRetry().doBeforeRetry { signal ->
            println("${this@DefaultBrokerClient}，尝试第 ${signal.totalRetries() + 1} 次重连...")
          }
        )
        .resume(
          Resume()
            .sessionDuration(Duration.ofMinutes(5))
            .retry(
              InternalKit.defaultRetry().doBeforeRetry { signal ->
                println("${this@DefaultBrokerClient}，尝试第 ${signal.totalRetries() + 1} 次重连...")
              }
            )
        )
        .connect(tcpClientTransport(target))
    )
  }

  override fun dispose() = rSocketClient.dispose()

  override fun isDisposed(): Boolean = rSocketClient.isDisposed

  override suspend fun fireAndForget(block: suspend () -> CloudEvent) {
    TODO("Not yet implemented")
  }

  override suspend fun requestResponse(block: suspend () -> CloudEvent): Mono<Payload> {
    TODO("Not yet implemented")
  }

  override suspend fun requestStream(block: suspend () -> CloudEvent): Flow<Payload> {
    TODO("Not yet implemented")
  }

  override suspend fun requestChannel(block: () -> Flow<CloudEvent>): Flow<Payload> {
    TODO("Not yet implemented")
  }

  override suspend fun metadataPush(block: suspend () -> CloudEvent) {
    TODO("Not yet implemented")
  }
}
