package com.github.ixtf.broker.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.JsonNode
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@JvmRecord
data class SetupDTO(
  val host: String,
  val service: String? = null,
  val instance: String? = null,
  val tags: Set<String>? = null,
  val token: String? = null,
  val extra: JsonNode? = null,
) : Serializable
