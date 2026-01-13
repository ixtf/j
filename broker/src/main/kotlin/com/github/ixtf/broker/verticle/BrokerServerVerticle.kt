package com.github.ixtf.broker.verticle

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.ixtf.broker.IXTF_API_BROKER_AUTH
import com.github.ixtf.broker.IXTF_API_BROKER_TARGET
import com.github.ixtf.broker.application.BrokerServerEntity
import com.github.ixtf.broker.domain.BrokerServer
import com.github.ixtf.core.J
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.vertx.core.ThreadingModel.VIRTUAL_THREAD
import io.vertx.ext.auth.authentication.AuthenticationProvider
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.ext.auth.jwt.jwtAuthOptionsOf
import io.vertx.kotlin.ext.auth.pubSecKeyOptionsOf

private val SERVER_CACHE = Caffeine.newBuilder().build<String, BrokerServerEntity>()

abstract class BrokerServerVerticle : BaseCoroutineVerticle() {
  protected val defaultAuthProvider by lazy {
    IXTF_API_BROKER_AUTH.takeIf { it.isNotBlank() }
      ?.let {
        JWTAuth.create(
          vertx,
          jwtAuthOptionsOf().apply {
            addPubSecKey(pubSecKeyOptionsOf(algorithm = "HS256").setBuffer(it))
          },
        )
      }
  }

  protected suspend fun addServer(
    id: String = J.objectId(),
    name: String = "Broker",
    target: String = IXTF_API_BROKER_TARGET,
    authProvider: AuthenticationProvider? = null,
  ) {
    val (host, port) = target.split(":")
    addServer(
      BrokerServer(
        id = id,
        name = name,
        host = host,
        port = port.toInt(),
        authProvider = authProvider,
      )
    )
  }

  protected suspend fun addServer(server: BrokerServer) {
    val entity = BrokerServerEntity(server = server)
    val options = deploymentOptionsOf(threadingModel = VIRTUAL_THREAD)
    vertx.deployVerticle(entity, options).coAwait()
    SERVER_CACHE.put(server.id, entity)
  }
}
