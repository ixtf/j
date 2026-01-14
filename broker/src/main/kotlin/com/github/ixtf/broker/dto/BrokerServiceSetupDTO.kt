package com.github.ixtf.broker.dto

import java.io.Serializable

@JvmRecord
data class BrokerServiceSetupDTO(
  val host: String,
  val service: String,
  val principal: String,
  val tags: Set<String>? = null,
  val token: String? = null,
) : Serializable
