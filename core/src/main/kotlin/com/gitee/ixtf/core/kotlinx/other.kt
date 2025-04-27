package com.gitee.ixtf.core.kotlinx
fun Throwable.rCause(): Throwable = cause?.rCause() ?: this
