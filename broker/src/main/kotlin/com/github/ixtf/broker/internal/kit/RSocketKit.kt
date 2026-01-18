package com.github.ixtf.broker.internal.kit

import cn.hutool.core.util.ReflectUtil
import cn.hutool.log.Log
import io.rsocket.DuplexConnection
import io.rsocket.RSocket
import java.net.SocketAddress

internal fun RSocket.remoteAddress(): SocketAddress? =
  runCatching { ReflectUtil.getFieldValue(this, "connection") }
    .map { (it as? DuplexConnection)?.remoteAddress() }
    .getOrDefault(null)

internal fun RSocket.doOnClose(log: Log, block: () -> Unit) {
  onClose().doOnError { log.error(it) }.doFinally { block() }.subscribe()
}
