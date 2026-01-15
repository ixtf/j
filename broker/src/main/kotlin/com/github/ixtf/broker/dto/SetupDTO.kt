package com.github.ixtf.broker.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@JvmRecord
data class SetupDTO(
  val host: String,
  val service: String? = null,
  val instance: String? = null,
  val tags: Set<String>? = null,
  val token: String? = null,
) : Serializable
