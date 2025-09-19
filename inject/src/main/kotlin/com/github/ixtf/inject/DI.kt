package com.github.ixtf.inject

import com.google.common.reflect.TypeToken
import io.avaje.inject.BeanScope
import java.lang.reflect.ParameterizedType
import java.util.Optional
import kotlin.reflect.KProperty

object DI {
  val BEAN_SCOPE by lazy { BeanScope.builder().build() }

  inline fun <reified T : Any> get(): T {
    if (T::class.java == Optional::class.java) {
      val typeToken = object : TypeToken<T>() {}
      val optionalInnerType = typeToken.type as ParameterizedType
      val realType = optionalInnerType.actualTypeArguments[0]
      return BEAN_SCOPE.getOptional<Any>(realType, null) as T
    }
    return BEAN_SCOPE.get(T::class.java)
  }

  inline operator fun <reified T : Any> getValue(thisRef: Any?, property: KProperty<*>): T =
      get<T>()
}
