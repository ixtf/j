package com.gitee.ixtf.core.kotlinx

import cn.hutool.core.io.FileUtil
import com.fasterxml.jackson.module.kotlin.readValue
import com.gitee.ixtf.core.J
import com.gitee.ixtf.core.MAPPER

fun String.filename(): String = FileUtil.getName(this)

fun String.mainName(): String = FileUtil.mainName(this)

fun String.extName(): String = FileUtil.extName(this)

inline fun <reified T> String.readJson(): T = MAPPER.readValue<T>(this)

inline fun <reified T> String.inputCommand(): T = J.inputCommand(readJson<T>())

inline fun <reified T> String.readJsonFile(): T = J.file(this).readJson<T>()

fun String.writeJson(o: Any) = J.file(this).writeJson(o)
