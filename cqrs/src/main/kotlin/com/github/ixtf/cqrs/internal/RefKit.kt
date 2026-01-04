package com.github.ixtf.cqrs.internal

import com.google.common.collect.Maps
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

internal object RefKit {
  private val METHOD_REF_CLASS = Maps.newConcurrentMap<KFunction<*>, Class<*>>()

  internal fun targetClass(methodRef: KFunction<*>) =
    METHOD_REF_CLASS.computeIfAbsent(methodRef) { _ ->
      val receiverClass = methodRef.parameters.firstOrNull()?.type?.classifier as? KClass<*>
      val actualClass = receiverClass?.java ?: methodRef.javaMethod?.declaringClass
      requireNotNull(actualClass) {
        "Method reference must have a receiver (e.g., XxxEntity::method)"
      }
    }

  internal inline fun <reified T> lookupFactory(
    factoryMap: Map<out Class<*>, T>,
    methodRef: KFunction<*>,
  ): T {
    val targetClass = targetClass(methodRef)
    return requireNotNull(factoryMap[targetClass]) {
      "No ComponentFactory bound for $targetClass (${methodRef.name})"
    }
  }

  //  internal fun lookupInvoker(instance: Any): ComponentInvoker<*> {
  //    val className = instance::class.java.name
  //    val invokerClassName = "${className}Invoker"
  //    val invokerClass = Class.forName(invokerClassName)
  //    return invokerClass.getConstructor(instance::class.java).newInstance(instance)
  //      as ComponentInvoker
  //  }
}
