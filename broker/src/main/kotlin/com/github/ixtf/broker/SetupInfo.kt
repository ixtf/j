package com.github.ixtf.broker

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.JsonNode
import com.github.ixtf.core.J
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@JvmRecord
data class SetupInfo(
  val service: String? = null,
  val instance: String? = null,
  val tags: Set<String>? = null,
  val host: String = J.localIp(),
  val extra: JsonNode? = null,
) : Serializable
