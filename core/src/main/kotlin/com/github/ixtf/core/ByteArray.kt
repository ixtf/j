package com.github.ixtf.core

import cn.hutool.core.codec.Base58
import cn.hutool.core.codec.Base62
import cn.hutool.core.codec.Base64
import cn.hutool.core.util.HexUtil
import cn.hutool.crypto.digest.DigestUtil
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.ByteArrayInputStream

fun ByteArray.gzip(): ByteArray = ByteArrayInputStream(this).gzip()

fun ByteArray.unGzip(): ByteArray = ByteArrayInputStream(this).unGzip()

inline fun <reified T> ByteArray.readJson(): T = MAPPER.readValue<T>(this)

inline fun <reified T> ByteArray.inputCommand(): T = J.inputCommand(readJson<T>())

fun ByteArray.base64(): String = Base64.encode(this)

fun ByteArray.base64UrlSafe(): String = Base64.encodeUrlSafe(this)

fun ByteArray.base64WithoutPadding(): String = Base64.encodeWithoutPadding(this)

fun ByteArray.base62(): String = Base62.encode(this)

fun ByteArray.base58(): String = Base58.encode(this)

fun ByteArray.hex(): String = HexUtil.encodeHexStr(this)

fun ByteArray.sm3(): ByteArray = DigestUtil.digester("sm3").digest(this)

fun ByteArray.sha256(): ByteArray = DigestUtil.sha256(this)

fun ByteArray.md5(): ByteArray = DigestUtil.md5(this)
