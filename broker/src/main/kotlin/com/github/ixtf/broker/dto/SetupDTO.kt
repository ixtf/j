package com.github.ixtf.broker.dto

import java.io.Serializable

@JvmRecord
data class SetupDTO(
  val host: String,
  val service: String? = null,
  val instance: String? = null,
  val tags: Set<String>? = null,
  val token: String? = null,
) : Serializable
