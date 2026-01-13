package com.github.ixtf.broker.application

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.ixtf.broker.domain.BrokerServer
import com.github.ixtf.broker.domain.event.BrokerServerEvent
import com.github.ixtf.broker.internal.doAfterTerminate
import com.github.ixtf.broker.toBuffer
import com.github.ixtf.core.MAPPER
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.rsocket.Closeable
import io.rsocket.ConnectionSetupPayload
import io.rsocket.RSocket
import io.rsocket.SocketAcceptor
import io.rsocket.core.RSocketServer
import io.rsocket.core.Resume
import io.rsocket.frame.decoder.PayloadDecoder
import io.vertx.kotlin.coroutines.receiveChannelHandler
import java.time.Duration
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

class BrokerServerEntity(private var server: BrokerServer) :
  BaseCoroutineVerticle(), SocketAcceptor {
  private val channel by lazy { vertx.receiveChannelHandler<BrokerServerEvent>() }
  private lateinit var closeable: Closeable

  override suspend fun start() {
    super.start()
    closeable =
      RSocketServer.create(this)
        .payloadDecoder(PayloadDecoder.ZERO_COPY)
        .resume(Resume().sessionDuration(Duration.ofMinutes(5)))
        .bind(server.transport())
        .awaitSingle()

    launch {
      channel.consumeEach { event ->
        try {
          server =
            when (event) {
              is BrokerServerEvent.Connected -> server.onEvent(event)
              is BrokerServerEvent.DisConnected -> server.onEvent(event)
            }
        } catch (_: CancellationException) {
          // ignore
        } catch (t: Throwable) {
          log.error(t, "state: {}", server)
        }
      }
    }
  }

  override fun accept(setup: ConnectionSetupPayload, sendingSocket: RSocket): Mono<RSocket> = mono {
    MAPPER.readValue<BrokerServerEvent>(setup.toBuffer().bytes)
    println(setup.refCnt())
    sendingSocket.doAfterTerminate {}
    server
  }
}
