package com.github.ixtf.core

import cn.hutool.core.compress.Gzip
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.io.ByteStreams
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets.UTF_8

inline fun <reified T> InputStream.readJson(): T = MAPPER.readValue<T>(this)

fun InputStream.bytes(): ByteArray = use { ByteStreams.toByteArray(this) }

fun InputStream.readTxt(): String = bytes().toString(UTF_8)

fun InputStream.gzip(): ByteArray = use { input ->
  ByteArrayOutputStream().use { output ->
    Gzip.of(input, output).gzip()
    output.toByteArray()
  }
}

fun InputStream.unGzip(): ByteArray = use { input ->
  ByteArrayOutputStream().use { output ->
    Gzip.of(input, output).unGzip()
    output.toByteArray()
  }
}
