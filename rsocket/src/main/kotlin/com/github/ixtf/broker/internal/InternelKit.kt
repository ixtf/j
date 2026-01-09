package com.github.ixtf.broker.internal

import io.cloudevents.CloudEvent
import io.rsocket.ConnectionSetupPayload
import io.rsocket.RSocket
import io.rsocket.transport.ClientTransport
import io.rsocket.transport.netty.client.TcpClientTransport
import reactor.core.scheduler.Schedulers

fun ConnectionSetupPayload.toCloudEvent(): CloudEvent {
  TODO()
}

internal fun tcpClientTransport(target: String): ClientTransport {
  val (bindAddress, port) = target.split(":")
  return TcpClientTransport.create(bindAddress, port.toInt())
}

fun RSocket.doAfterTerminate(block: () -> Unit) {
  onClose().doAfterTerminate(block).subscribeOn(Schedulers.boundedElastic()).subscribe()
}
