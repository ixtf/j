package com.github.ixtf.core.kotlinx

import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.file.PathUtil
import cn.hutool.crypto.digest.DigestUtil
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.ixtf.core.J
import com.github.ixtf.core.objectMap
import java.io.File
import java.nio.file.Path

fun File.filename(): String = FileUtil.getName(this)

fun Path.filename(): String = PathUtil.getName(this)

fun File.mainName(): String = FileUtil.mainName(this)

fun Path.mainName(): String = FileUtil.mainName(toFile())

fun File.extName(): String = FileUtil.extName(this)

fun Path.extName(): String = FileUtil.extName(toFile())

fun File.md5Hex(): String = DigestUtil.md5Hex(this)

fun Path.md5Hex(): String = DigestUtil.md5Hex(toFile())

fun File.sha256Hex(): String = DigestUtil.sha256Hex(this)

fun Path.sha256Hex(): String = DigestUtil.sha256Hex(toFile())

fun File.sm3Hex(): String = DigestUtil.digester("sm3").digestHex(this)

fun Path.sm3Hex(): String = DigestUtil.digester("sm3").digestHex(toFile())

inline fun <reified T> File.readJson(): T = objectMap(filename()).readValue<T>(this)

inline fun <reified T> Path.readJson(): T = objectMap(filename()).readValue<T>(toFile())

inline fun <reified T> File.inputCommand(): T = J.inputCommand(readJson<T>())

inline fun <reified T> Path.inputCommand(): T = J.inputCommand(readJson<T>())

fun File.writeJson(o: Any) {
  FileUtil.mkParentDirs(this)
  objectMap(filename()).writerWithDefaultPrettyPrinter().writeValue(this, o)
}

fun Path.writeJson(o: Any) {
  PathUtil.mkParentDirs(this)
  objectMap(filename()).writerWithDefaultPrettyPrinter().writeValue(toFile(), o)
}
