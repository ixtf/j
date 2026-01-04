package com.github.ixtf.compiler.ksp.generator

import com.github.ixtf.compiler.ksp.CODEGEN_PACKAGE
import com.github.ixtf.compiler.ksp.MODULE_NAME
import com.github.ixtf.compiler.ksp.model.ComponentDescriptor
import com.github.ixtf.cqrs.internal.codegen.CodegenCqrsModule
import com.github.ixtf.cqrs.internal.codegen.ComponentInvoker
import com.github.ixtf.cqrs.internal.codegen.EventSourcedEntityFactory
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*
import dagger.*
import dagger.multibindings.IntoSet

class CqrsModuleGenerator(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) {
  fun generate(descriptors: List<ComponentDescriptor>, originatingFiles: Set<KSFile>) {
    if (descriptors.isEmpty()) return

    val moduleType =
      TypeSpec.Companion.interfaceBuilder(MODULE_NAME)
        .addModifiers(KModifier.PUBLIC)
        .addAnnotation(
          AnnotationSpec.builder(Module::class)
            .addMember("includes = [%T::class]", CodegenCqrsModule::class)
            .build()
        )

    descriptors.forEach { descriptor ->
      moduleType.addFunction(bindFactory(descriptor))
      moduleType.addFunction(bindInvoker(descriptor))
    }

    FileSpec.builder(CODEGEN_PACKAGE, MODULE_NAME)
      .addType(moduleType.build())
      .build()
      .writeTo(codeGenerator, Dependencies(aggregating = true, *originatingFiles.toTypedArray()))
  }

  private fun bindFactory(descriptor: ComponentDescriptor): FunSpec {
    val factoryClass = ClassName(CODEGEN_PACKAGE, "${descriptor.simpleName}Factory")

    return FunSpec.builder("bind${descriptor.simpleName}Factory")
      .addModifiers(KModifier.PUBLIC, KModifier.ABSTRACT)
      .addAnnotation(Binds::class)
      .addAnnotation(IntoSet::class)
      .addParameter("factory", factoryClass)
      .returns(EventSourcedEntityFactory::class.asTypeName().parameterizedBy(STAR))
      .build()
  }

  private fun bindInvoker(descriptor: ComponentDescriptor): FunSpec {
    val invokerClass = ClassName(CODEGEN_PACKAGE, "${descriptor.simpleName}Invoker")

    return FunSpec.builder("bind${descriptor.simpleName}Invoker")
      .addModifiers(KModifier.PUBLIC, KModifier.ABSTRACT)
      .addAnnotation(Binds::class)
      .addAnnotation(IntoSet::class)
      .addParameter("invoker", invokerClass)
      .returns(ComponentInvoker::class.asTypeName().parameterizedBy(STAR))
      .build()
  }
}
