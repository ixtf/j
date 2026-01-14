package com.github.ixtf.vertx

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.ixtf.core.MAPPER
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.nio.charset.StandardCharsets

inline fun <reified T> Buffer.readValueOrNull(): T? {
  if (bytes.isEmpty()) return null
  return when (T::class) {
    Buffer::class -> this
    ByteArray::class -> bytes
    JsonObject::class -> toJsonArray()
    JsonArray::class -> toJsonArray()
    String::class -> toString(StandardCharsets.UTF_8)
    else -> MAPPER.readValue(bytes)
  }
    as T
}

inline fun <reified T> Buffer.readValue(): T = requireNotNull(readValueOrNull())
