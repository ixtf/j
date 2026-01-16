package com.github.ixtf.broker.internal.kit

import io.rsocket.transport.ServerTransport
import io.rsocket.transport.netty.server.TcpServerTransport

@JvmInline
internal value class ServerTarget(val target: String) {
  fun transport(): ServerTransport<*> {
    val (bindAddress, port) = target.split(":")
    return TcpServerTransport.create(bindAddress, port.toInt())
  }
}
