package com.github.ixtf.core

import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.file.PathUtil
import cn.hutool.crypto.digest.DigestUtil
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.nio.file.Path

fun File.filename(): String = FileUtil.getName(this)

fun Path.filename(): String = PathUtil.getName(this)

fun File.mainName(): String = FileUtil.mainName(this)

fun Path.mainName(): String = FileUtil.mainName(toFile())

fun File.extName(): String = FileUtil.extName(this)

fun Path.extName(): String = FileUtil.extName(toFile())

fun File.md5(): ByteArray = DigestUtil.md5(this)

fun Path.md5(): ByteArray = DigestUtil.md5(toFile())

fun File.sha256(): ByteArray = DigestUtil.sha256(this)

fun Path.sha256(): ByteArray = DigestUtil.sha256(toFile())

fun File.sm3(): ByteArray = DigestUtil.digester("sm3").digest(this)

fun Path.sm3(): ByteArray = DigestUtil.digester("sm3").digest(toFile())

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
