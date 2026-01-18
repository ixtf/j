package com.github.ixtf.broker.internal.kit

import io.rsocket.transport.netty.client.TcpClientTransport

@JvmInline
internal value class ClientTarget(val value: String) {
  fun transport(): TcpClientTransport {
    val (bindAddress, port) = value.split(":")
    return TcpClientTransport.create(bindAddress, port.toInt())
  }
}
