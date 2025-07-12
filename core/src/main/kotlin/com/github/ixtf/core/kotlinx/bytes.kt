package com.github.ixtf.core.kotlinx

import cn.hutool.core.codec.Base58
import cn.hutool.core.compress.Gzip
import cn.hutool.crypto.digest.DigestUtil
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.ixtf.core.J
import com.github.ixtf.core.MAPPER
import com.google.common.io.ByteStreams
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets.UTF_8

fun InputStream.gzip(): ByteArray = use { input ->
  ByteArrayOutputStream().use { output ->
    Gzip.of(input, output).gzip()
    output.toByteArray()
  }
}

fun ByteArray.gzip(): ByteArray = ByteArrayInputStream(this).gzip()

fun InputStream.unGzip(): ByteArray = use { input ->
  ByteArrayOutputStream().use { output ->
    Gzip.of(input, output).unGzip()
    output.toByteArray()
  }
}

fun ByteArray.unGzip(): ByteArray = ByteArrayInputStream(this).unGzip()

fun InputStream.bytes(): ByteArray = use { ByteStreams.toByteArray(this) }

fun InputStream.readTxt(): String = bytes().toString(UTF_8)

inline fun <reified T> InputStream.readJson(): T = MAPPER.readValue<T>(this)

inline fun <reified T> ByteArray.readJson(): T = MAPPER.readValue<T>(this)

inline fun <reified T> ByteArray.inputCommand(): T = J.inputCommand(readJson<T>())

fun ByteArray.base58(): String = Base58.encode(this)

fun ByteArray.md5Hex(): String = DigestUtil.md5Hex(this)

fun ByteArray.sha256Hex(): String = DigestUtil.sha256Hex(this)

fun ByteArray.sm3Hex(): String = DigestUtil.digester("sm3").digestHex(this)
