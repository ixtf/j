package com.github.ixtf.compiler.ksp

import com.github.ixtf.compiler.ksp.model.ComponentDescriptor
import com.github.ixtf.compiler.ksp.model.MethodDescriptor
import com.github.ixtf.compiler.ksp.model.ParameterDescriptor
import com.github.ixtf.cqrs.eventsourcedentity.EventSourcedEntity
import com.github.ixtf.cqrs.keyvalueentity.KeyValueEntity
import com.github.ixtf.cqrs.workflow.Workflow
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.*

const val CODEGEN_PACKAGE = "com.github.ixtf.codegen"
const val MODULE_NAME = "CqrsModule"

fun KSClassDeclaration.toDescriptor(): ComponentDescriptor? {
  val qualifiedName = qualifiedName?.asString() ?: return null
  val packageName = packageName.asString()
  val simpleName = simpleName.asString()

  val kind =
    when {
      isEventSourcedEntity() -> ComponentDescriptor.Kind.EVENT_SOURCED
      isKeyValueEntity() -> ComponentDescriptor.Kind.KEY_VALUE
      isWorkflow() -> ComponentDescriptor.Kind.WORKFLOW
      else -> return null
    }

  return ComponentDescriptor(
    packageName = packageName,
    simpleName = simpleName,
    qualifiedName = qualifiedName,
    kind = kind,
    methods = collectMethods(),
    //    methods = emptyList(),
    originatingFile = containingFile,
  )
}

fun KSClassDeclaration.collectMethods(): List<MethodDescriptor> {
  return getAllFunctions().filter { it.isEligibleMethod() }.map { it.toMethodDescriptor() }.toList()
}

private fun KSFunctionDeclaration.isEligibleMethod(): Boolean {

  // 排除构造函数
  if (isConstructor()) return false

  // 只处理 public
  // if (!modifiers.contains(Modifier.PUBLIC)) return false
  if (getVisibility() != Visibility.PUBLIC) return false

  // 排除 abstract（接口方法、基类方法）
  if (modifiers.contains(Modifier.ABSTRACT)) return false

  // 排除 synthetic / compiler generated
  if (origin == Origin.SYNTHETIC) return false

  if (!modifiers.contains(Modifier.SUSPEND)) return false

  // 排除函数级泛型
  if (typeParameters.isNotEmpty()) {
    println(
      "Skip generic function '${simpleName.asString()}': " +
        "function-level type parameters are not supported by codegen"
    )
    return false
  }

  // 排除 Any 的方法
  val name = simpleName.asString()
  if (name in setOf("equals", "hashCode", "toString")) return false

  //  if (name in setOf("start", "stop", "init","deploy","undeploy",
  //      "getVertx","coHandler","applyEvent","coErrorHandler","coFailureHandler")) return false

  return true
}

private fun KSFunctionDeclaration.toMethodDescriptor(): MethodDescriptor {
  val methodName = simpleName.asString()

  val parameters =
    parameters.map { param ->
      ParameterDescriptor(name = param.name?.asString() ?: "_", type = param.type.toTypeName())
    }

  val returnType: TypeName = this.returnType?.toTypeName() ?: UNIT

  return MethodDescriptor(
    name = methodName,
    parameters = parameters,
    isSuspend = modifiers.contains(Modifier.SUSPEND),
    returnType = returnType,
  )
}

private fun KSClassDeclaration.isEventSourcedEntity(): Boolean =
  hasSuperType(EventSourcedEntity::class.java.name)

private fun KSClassDeclaration.isKeyValueEntity(): Boolean =
  hasSuperType(KeyValueEntity::class.java.name)

private fun KSClassDeclaration.isWorkflow(): Boolean = hasSuperType(Workflow::class.java.name)

private fun KSClassDeclaration.hasSuperType(fqcn: String): Boolean =
  getAllSuperTypes().any { type -> type.declaration.qualifiedName?.asString() == fqcn }

fun KSClassDeclaration.isConcreteComponent(): Boolean {
  if (classKind == ClassKind.INTERFACE) return false
  if (modifiers.contains(Modifier.ABSTRACT)) return false

  return getAllSuperTypes().any { superType ->
    val qName = superType.declaration.qualifiedName?.asString() ?: return@any false
    qName.startsWith("com.github.ixtf.cqrs")
  }
}

fun KSClassDeclaration.isConcreteSubTypeOf(base: KSClassDeclaration): Boolean {
  if (modifiers.contains(Modifier.ABSTRACT)) return false

  return superTypes.any { superType ->
    val resolved = superType.resolve().declaration
    resolved == base || (resolved is KSClassDeclaration && resolved.isConcreteSubTypeOf(base))
  }
}

fun KSClassDeclaration.isExcluded(excludedPackages: List<String>): Boolean =
  excludedPackages.any { packageName.asString().startsWith(it) }
