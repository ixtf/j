package com.github.ixtf.broker

import java.io.Serializable

@JvmRecord
data class RouteOptions(
  val service: String? = null,
  val instance: String? = null,
  val tags: Set<String>? = null,
  val token: String? = null,
) : Serializable
