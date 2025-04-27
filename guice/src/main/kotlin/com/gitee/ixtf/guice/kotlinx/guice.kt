package com.gitee.ixtf.guice.kotlinx

import com.gitee.ixtf.guice.Jguice
import com.google.inject.Key

inline fun <reified T> Jguice.get(): T = getInstance(object : Key<T>() {})

inline fun <reified T> Key<T>.get(): T = Jguice.getInstance(this)

inline fun <reified T> Jguice.get(name: String): T = getInstance(T::class.java, name)

inline fun <reified T> Jguice.get(annotation: Annotation): T =
    getInstance(T::class.java, annotation)

inline fun <reified T> Jguice.get(annotationType: Class<out Annotation>): T =
    getInstance(T::class.java, annotationType)
