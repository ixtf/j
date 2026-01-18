package com.github.ixtf.compiler.ksp.model

import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.TypeName

data class ComponentDescriptor(
  val packageName: String,
  val simpleName: String,
  val qualifiedName: String,
  val kind: Kind,
  val methods: List<MethodDescriptor>,
  val originatingFile: KSFile?,
) {
  enum class Kind {
    EVENT_SOURCED,
    KEY_VALUE,
    WORKFLOW,
  }
}

data class MethodDescriptor(
  val name: String,
  val parameters: List<ParameterDescriptor>,
  val isSuspend: Boolean,
  val returnType: TypeName,
)

data class ParameterDescriptor(val name: String, val type: TypeName)
