package com.github.ixtf.broker.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.JsonNode
import com.github.ixtf.broker.internal.InternalKit.defaultAuth
import com.github.ixtf.core.J
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@JvmRecord
data class SetupDTO(
  val service: String? = null,
  val instance: String? = null,
  val tags: Set<String>? = null,
  val host: String = J.localIp(),
  val extra: JsonNode? = null,
) : Serializable {
  companion object {
    fun JWTAuth.brokerToken(dto: SetupDTO): String = generateToken(JsonObject.mapFrom(dto))

    fun Vertx.brokerToken(dto: SetupDTO): String = defaultAuth().brokerToken(dto)

    fun Vertx.brokerToken(dto: SetupDTO, buffer: String): String =
      defaultAuth(buffer).brokerToken(dto)
  }
}
