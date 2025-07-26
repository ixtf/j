@file:Suppress("unused")

package com.github.ixtf.core.kotlinx

import cn.hutool.core.util.StrUtil.EMPTY
import com.github.ixtf.core.J
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class IxtfEnvString(sysProp: String, v: String = EMPTY) : IxtfEnv<String>(sysProp, v) {
  override fun getValue(thisRef: Any?, property: KProperty<*>) = read()
}

class IxtfEnvPath(sysProp: String, v: String = EMPTY) : IxtfEnv<Path>(sysProp, v) {
  override fun getValue(thisRef: Any?, property: KProperty<*>) = Path(read())
}

class IxtfEnvInt(sysProp: String, v: Int) : IxtfEnv<Int>(sysProp, v.toString()) {
  override fun getValue(thisRef: Any?, property: KProperty<*>) = read().toInt()
}

class IxtfEnvLong(sysProp: String, v: Long) : IxtfEnv<Long>(sysProp, v.toString()) {
  override fun getValue(thisRef: Any?, property: KProperty<*>) = read().toLong()
}

class IxtfEnvDouble(sysProp: String, v: Double) : IxtfEnv<Double>(sysProp, v.toString()) {
  override fun getValue(thisRef: Any?, property: KProperty<*>) = read().toDouble()
}

class IxtfEnvFloat(sysProp: String, v: Float) : IxtfEnv<Float>(sysProp, v.toString()) {
  override fun getValue(thisRef: Any?, property: KProperty<*>) = read().toFloat()
}

abstract class IxtfEnv<T>(val sysProp: String, val defaultV: String) : ReadWriteProperty<Any?, T> {
  private val envProp by lazy { sysProp.replace('.', '_').uppercase() }

  protected fun read(): String {
    var v = System.getProperty(sysProp)
    if (J.nonBlank(v)) return v
    v = System.getenv(envProp)
    if (J.nonBlank(v)) return v
    return defaultV
  }

  protected fun write(v: String) {
    System.setProperty(sysProp, v)
  }

  override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
      write(value.toString())
}
