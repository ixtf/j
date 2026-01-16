package com.github.ixtf.broker

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.JsonNode
import com.github.ixtf.broker.Env.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.internal.DefaultBrokerClient
import com.github.ixtf.broker.internal.InternalKit.defaultAuth
import com.github.ixtf.core.J
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import java.io.Serializable

interface BrokerClient : NativeClient {
  fun route(route: BrokerRouteOptions): BrokerRoute

  companion object {
    fun create(vertx: Vertx, token: String, target: String = IXTF_API_BROKER_TARGET): BrokerClient =
      DefaultBrokerClient(vertx, token, target)
  }
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JvmRecord
data class SetupInfo(
  val service: String? = null,
  val instance: String? = null,
  val tags: Set<String>? = null,
  val host: String = J.localIp(),
  val extra: JsonNode? = null,
) : Serializable {
  companion object {
    fun JWTAuth.brokerToken(dto: SetupInfo): String = generateToken(JsonObject.mapFrom(dto))

    fun Vertx.brokerToken(dto: SetupInfo): String = defaultAuth().brokerToken(dto)

    fun Vertx.brokerToken(dto: SetupInfo, buffer: String): String =
      defaultAuth(buffer).brokerToken(dto)
  }
}