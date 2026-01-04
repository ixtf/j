package com.github.ixtf.compiler.ksp.generator

import com.github.ixtf.compiler.ksp.CODEGEN_PACKAGE
import com.github.ixtf.compiler.ksp.model.ComponentDescriptor
import com.github.ixtf.cqrs.internal.codegen.EventSourcedEntityFactory
import com.github.ixtf.cqrs.keyvalueentity.KeyValueEntity
import com.github.ixtf.cqrs.workflow.Workflow
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*
import dagger.assisted.*

/**
 * template:
 *
 * ```kotlin
 * @AssistedFactory
 * interface ${name}Factory : EventSourcedEntityFactory<$name> {
 *   override val componentClass: Class<$name> get() = $name::class.java
 * }
 * ```
 */
class FactoryGenerator(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) {
  fun generate(descriptors: List<ComponentDescriptor>, originatingFiles: Set<KSFile>) {
    descriptors.forEach { descriptor ->
      val superInterface =
        when (descriptor.kind) {
          ComponentDescriptor.Kind.EVENT_SOURCED -> EventSourcedEntityFactory::class.asClassName()
          ComponentDescriptor.Kind.KEY_VALUE -> KeyValueEntity::class.asClassName()
          ComponentDescriptor.Kind.WORKFLOW -> Workflow::class.asClassName()
        }
      generateFactory(descriptor, superInterface, originatingFiles)
    }
  }

  private fun generateFactory(
    descriptor: ComponentDescriptor,
    factorySuperInterface: ClassName,
    originatingFiles: Set<KSFile>,
  ) {
    val factoryName = "${descriptor.simpleName}Factory"
    val componentClass = ClassName.bestGuess(descriptor.qualifiedName)

    val componentClassProperty =
      PropertySpec.builder(
          "componentClass",
          Class::class.asTypeName().parameterizedBy(componentClass),
        )
        .addModifiers(KModifier.OVERRIDE)
        .getter(
          FunSpec.getterBuilder().addStatement("return %T::class.java", componentClass).build()
        )
        .build()

    val factoryInterface =
      TypeSpec.interfaceBuilder(factoryName)
        .addModifiers(KModifier.PUBLIC)
        .addAnnotation(AssistedFactory::class)
        .addSuperinterface(factorySuperInterface.parameterizedBy(componentClass))
        .addProperty(componentClassProperty)
        .build()

    FileSpec.builder(CODEGEN_PACKAGE, factoryName)
      .addType(factoryInterface)
      .build()
      .writeTo(codeGenerator, Dependencies(aggregating = false, *originatingFiles.toTypedArray()))
  }
}
