package com.github.ixtf.cqrs.internal.codegen

import com.github.ixtf.cqrs.eventsourcedentity.EventSourcedEntity

interface ComponentInvoker<T : EventSourcedEntity<*, *>> : ComponentFactory<T> {
  suspend fun invoke(instance: T, methodName: String, args: Array<out Any?>): Any?
}
