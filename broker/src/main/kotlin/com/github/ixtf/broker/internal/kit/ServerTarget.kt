package com.github.ixtf.broker.internal.kit

import io.rsocket.transport.ServerTransport
import io.rsocket.transport.netty.server.TcpServerTransport

@JvmInline
internal value class ServerTarget(val value: String) {
  fun transport(): ServerTransport<*> {
    val (bindAddress, port) = value.split(":")
    return TcpServerTransport.create(bindAddress, port.toInt())
  }
}
