@file:Suppress("unused")

package com.gitee.ixtf.core

import cn.hutool.core.exceptions.ValidateException
import cn.hutool.core.lang.Validator

object Jassert {
  /** @see Validator.validateTrue */
  @JvmStatic
  fun isTrue(o: Boolean, block: () -> String = { "isTrue Temp" }) =
      if (Validator.isFalse(o)) throw ValidateException(block()) else true

  /** @see Validator.validateFalse */
  @JvmStatic
  fun isFalse(o: Boolean, block: () -> String = { "isFalse Temp" }) =
      if (Validator.isTrue(o)) throw ValidateException(block()) else false

  /** @see Validator.validateNull */
  @JvmStatic
  fun <T> isNull(o: T?, block: () -> String = { "isFalse Temp" }): T? =
      if (Validator.isNotNull(o)) throw ValidateException(block()) else o

  /** @see Validator.validateNotNull */
  @JvmStatic
  fun <T> nonNull(o: T?, block: () -> String = { "isFalse Temp" }): T =
      if (Validator.isNull(o)) throw ValidateException(block()) else o!!

  @JvmStatic
  fun <T : CharSequence> isBlank(o: T?, block: () -> String = { "isBlank Temp" }): T? {
    isTrue(J.isBlank(o), block)
    return o
  }

  @JvmStatic
  fun <T : CharSequence> nonBlank(o: T?, block: () -> String = { "isBlank Temp" }): T {
    isFalse(J.isBlank(o), block)
    return o!!
  }

  @JvmStatic
  fun isEmpty(o: CharSequence?, block: () -> String = { "isBlank Temp" }) =
      Validator.validateEmpty(o, block())

  @JvmStatic
  fun nonEmpty(o: CharSequence?, block: () -> String = { "isBlank Temp" }) =
      Validator.validateNotEmpty(o, block())

  @JvmStatic
  fun <T : Iterable<*>> isEmpty(o: T?, block: () -> String = { "isBlank Temp" }): T? {
    isTrue(J.isEmpty(o), block)
    return o
  }

  @JvmStatic
  fun <T : Iterable<*>> nonEmpty(o: T?, block: () -> String = { "isBlank Temp" }): T {
    isFalse(J.isEmpty(o), block)
    return o!!
  }

  @JvmStatic
  fun <T : Map<*, *>> isEmpty(o: T?, block: () -> String = { "isBlank Temp" }): T? {
    isTrue(J.isEmpty(o), block)
    return o
  }

  @JvmStatic
  fun <T : Map<*, *>> nonEmpty(o: T?, block: () -> String = { "isBlank Temp" }): T {
    isFalse(J.isEmpty(o), block)
    return o!!
  }

  @JvmStatic
  fun <T> isEmpty(o: Array<T>?, block: () -> String = { "isBlank Temp" }): Array<T>? {
    isTrue(J.isEmpty(o), block)
    return o
  }

  @JvmStatic
  fun <T> nonEmpty(o: Array<T>?, block: () -> String = { "isBlank Temp" }): Array<T> {
    isFalse(J.isEmpty(o), block)
    return o!!
  }
}
