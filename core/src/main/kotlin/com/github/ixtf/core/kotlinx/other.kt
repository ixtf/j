package com.github.ixtf.core.kotlinx

fun Throwable.rCause(): Throwable = cause?.rCause() ?: this
