package com.github.ixtf.broker

import com.github.ixtf.broker.Env.IXTF_API_BROKER_TARGET
import com.github.ixtf.core.J
import java.io.Serializable

@JvmRecord
data class BrokerClientOptions(
  val host: String = J.localIp(),
  val service: String? = null,
  val instance: String? = null,
  val tags: Set<String>? = null,
  val token: String? = null,
  val target: String = IXTF_API_BROKER_TARGET,
) : Serializable
