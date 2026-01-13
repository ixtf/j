package com.github.ixtf.broker.internal

import io.rsocket.RSocket
import io.rsocket.transport.ClientTransport
import io.rsocket.transport.ServerTransport
import io.rsocket.transport.netty.client.TcpClientTransport
import io.rsocket.transport.netty.server.TcpServerTransport
import reactor.core.scheduler.Schedulers

internal fun tcpServerTransport(target: String): ServerTransport<*> {
  val (bindAddress, port) = target.split(":")
  return TcpServerTransport.create(bindAddress, port.toInt())
}

internal fun tcpClientTransport(target: String): ClientTransport {
  val (bindAddress, port) = target.split(":")
  return TcpClientTransport.create(bindAddress, port.toInt())
}

internal fun RSocket.doAfterTerminate(block: () -> Unit) {
  onClose().doAfterTerminate(block).subscribeOn(Schedulers.boundedElastic()).subscribe()
}
