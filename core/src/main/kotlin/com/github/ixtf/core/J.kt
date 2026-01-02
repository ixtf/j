package com.github.ixtf.core

import cn.hutool.core.collection.CollUtil
import cn.hutool.core.collection.IterUtil
import cn.hutool.core.date.DateUtil
import cn.hutool.core.date.TimeInterval
import cn.hutool.core.map.MapUtil
import cn.hutool.core.util.ArrayUtil
import cn.hutool.core.util.IdUtil
import cn.hutool.core.util.StrUtil
import jakarta.validation.ConstraintViolationException
import java.io.File
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

object J {
  private lateinit var DI: Any

  @Synchronized
  fun initDI(block: () -> Any) {
    require(!::DI.isInitialized)
    DI = block()
  }

  @Suppress("UNCHECKED_CAST") fun <T> asDI(): T = DI as T

  fun timer(): TimeInterval = DateUtil.timer()

  @OptIn(ExperimentalContracts::class)
  inline fun <T> timer(s: String, block: () -> T): T {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    val timer = timer()
    val ret = block()
    println("$s: ${timer.intervalPretty()}")
    return ret
  }

  fun writeJson(s: String, o: Any) = File(s).writeJson(o)

  fun <T> inputCommand(o: T): T =
      o.apply {
        val violations = VALIDATOR.validate(o)
        if (violations.isNotEmpty()) throw ConstraintViolationException(violations)
      }

  fun blankToDefault(o: CharSequence?, default: String = StrUtil.EMPTY): String =
      StrUtil.blankToDefault(o, default)

  @OptIn(ExperimentalContracts::class)
  fun isBlank(o: CharSequence?): Boolean {
    contract { returns(false) implies (o != null) }
    return StrUtil.isBlank(o)
  }

  @OptIn(ExperimentalContracts::class)
  fun nonBlank(o: CharSequence?): Boolean {
    contract { returns(true) implies (o != null) }
    return !isBlank(o)
  }

  @OptIn(ExperimentalContracts::class)
  fun isEmpty(o: Collection<*>?): Boolean {
    contract { returns(false) implies (o != null) }
    return CollUtil.isEmpty(o)
  }

  @OptIn(ExperimentalContracts::class)
  fun nonEmpty(o: Collection<*>?): Boolean {
    contract { returns(true) implies (o != null) }
    return !isEmpty(o)
  }

  @OptIn(ExperimentalContracts::class)
  fun isEmpty(o: Iterable<*>?): Boolean {
    contract { returns(false) implies (o != null) }
    return IterUtil.isEmpty(o)
  }

  @OptIn(ExperimentalContracts::class)
  fun nonEmpty(o: Iterable<*>?): Boolean {
    contract { returns(true) implies (o != null) }
    return !isEmpty(o)
  }

  @OptIn(ExperimentalContracts::class)
  fun isEmpty(o: Map<*, *>?): Boolean {
    contract { returns(false) implies (o != null) }
    return MapUtil.isEmpty(o)
  }

  @OptIn(ExperimentalContracts::class)
  fun nonEmpty(o: Map<*, *>?): Boolean {
    contract { returns(true) implies (o != null) }
    return !isEmpty(o)
  }

  @OptIn(ExperimentalContracts::class)
  fun isEmpty(o: Array<*>?): Boolean {
    contract { returns(false) implies (o != null) }
    return ArrayUtil.isEmpty(o)
  }

  @OptIn(ExperimentalContracts::class)
  fun nonEmpty(o: Array<*>?): Boolean {
    contract { returns(true) implies (o != null) }
    return !isEmpty(o)
  }

  fun objectId(): String = IdUtil.objectId()
}
