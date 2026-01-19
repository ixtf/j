package com.github.ixtf.broker.internal.kit

import com.github.ixtf.broker.IXTF_BROKER_AUTH
import io.vertx.core.Vertx
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.kotlin.ext.auth.jwt.jwtAuthOptionsOf
import io.vertx.kotlin.ext.auth.pubSecKeyOptionsOf

internal fun Vertx.defaultAuth(buffer: String = IXTF_BROKER_AUTH): JWTAuth {
  require(buffer.isNotBlank())
  val key = pubSecKeyOptionsOf(algorithm = "HS256").setBuffer(buffer)
  val config = jwtAuthOptionsOf(pubSecKeys = listOf(key))
  return JWTAuth.create(this, config)
}
