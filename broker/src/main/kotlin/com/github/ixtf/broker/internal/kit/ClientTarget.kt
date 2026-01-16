package com.github.ixtf.broker.internal.kit

import io.rsocket.transport.netty.client.TcpClientTransport

@JvmInline
internal value class ClientTarget(val target: String) {
  fun transport(): TcpClientTransport {
    val (bindAddress, port) = target.split(":")
    return TcpClientTransport.create(bindAddress, port.toInt())
  }
}
