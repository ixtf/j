package com.github.ixtf.core

import cn.hutool.core.io.FileUtil
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

fun String.filename(): String = FileUtil.getName(this)

fun String.mainName(): String = FileUtil.mainName(this)

fun String.extName(): String = FileUtil.extName(this)

inline fun <reified T> String.readJson(): T = MAPPER.readValue<T>(this)

inline fun <reified T> String.inputCommand(): T = J.inputCommand(readJson<T>())

inline fun <reified T> String.readJsonFile(): T = File(this).readJson<T>()

fun String.writeJson(o: Any) = File(this).writeJson(o)
