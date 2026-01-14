package com.github.ixtf.broker

import java.io.Serializable

@JvmRecord
data class BrokerClientOptions(
  val host: String? = null,
  val service: String? = null,
  val instance: String? = null,
  val tags: Set<String>? = null,
  val token: String? = null,
  val target: String? = null,
) : Serializable
