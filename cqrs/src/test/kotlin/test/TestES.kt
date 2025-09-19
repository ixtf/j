package test

import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.javaMethod

suspend fun main() {
  val methodRef = InstanceClass::instance
  val javaMethod = methodRef.javaMethod
  println(javaMethod)
  val clazz = javaMethod?.declaringClass
  println(clazz)
  val instance = InstanceClass()
  val v = if (methodRef.isSuspend) methodRef.callSuspend(instance) else methodRef.call(instance)
  println(v)
}

abstract class BaseClass<T> {
  abstract suspend fun instance(): T
}

class InstanceClass : BaseClass<String>() {
  override suspend fun instance(): String = "TEST"
}
