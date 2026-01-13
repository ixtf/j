package com.github.ixtf.broker.dto

import java.io.Serializable

@JvmRecord
data class SetupDTO(
  val token: String,
  val host: String,
  val service: String? = null,
  val tags: Set<String>? = null,
) : Serializable
