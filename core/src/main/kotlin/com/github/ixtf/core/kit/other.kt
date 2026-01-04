package com.github.ixtf.core.kit

fun Throwable.rCause(): Throwable = cause?.rCause() ?: this
