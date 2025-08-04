package com.github.ixtf.inject

import io.avaje.inject.BeanScope
import kotlin.reflect.KProperty

object DI {
  val BEAN_SCOPE by lazy { BeanScope.builder().build() }

  inline fun <reified T> get(): T = BEAN_SCOPE.get(T::class.java)

  inline operator fun <reified T> getValue(thisRef: Any?, property: KProperty<*>): T = get<T>()
}
