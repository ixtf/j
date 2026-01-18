package com.github.ixtf.cqrs.internal.codegen

sealed interface ComponentFactory<T : Any> {
  val componentClass: Class<T>
}
