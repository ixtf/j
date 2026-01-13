package com.github.ixtf.broker.dto

import java.io.Serializable

@JvmRecord
data class RouteDTO(
  val host: String,
  val service: String? = null,
  val principal: String? = null,
  val tags: Set<String>? = null,
  val token: String? = null,
) : Serializable
