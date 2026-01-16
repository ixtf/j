package com.github.ixtf.broker

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.JsonNode
import com.github.ixtf.broker.Env.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.internal.ClientTarget
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
    fun JWTAuth.brokerToken(info: SetupInfo): String = generateToken(JsonObject.mapFrom(info))

    fun Vertx.brokerToken(info: SetupInfo): String = defaultAuth().brokerToken(info)

    fun create(vertx: Vertx, token: String, target: String = IXTF_API_BROKER_TARGET): BrokerClient =
      DefaultBrokerClient(vertx, token, ClientTarget(target))
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
) : Serializable
