package com.github.ixtf.broker

import io.rsocket.RSocket

object Constant {
  const val BROKER_ROUTE = "__BROKER__"

  val EMPTY_RSOCKET = object : RSocket {}
}
