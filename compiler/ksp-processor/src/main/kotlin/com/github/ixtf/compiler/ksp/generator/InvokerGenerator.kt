package com.github.ixtf.compiler.ksp.generator

import com.github.ixtf.compiler.ksp.CODEGEN_PACKAGE
import com.github.ixtf.compiler.ksp.model.ComponentDescriptor
import com.github.ixtf.cqrs.internal.codegen.ComponentInvoker
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*
import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * Invoker 代码生成示意。
 *
 * KotlinPoet 将为每个 EventSourced Entity 生成一个 Invoker，实现 `ComponentInvoker<T>`，用于根据方法名动态分发调用。
 *
 * 生成后的代码结构如下：
 *
 * ```kotlin
 * @Singleton
 * class ExampleInvoker @Inject constructor() : ComponentInvoker<Example> {
 *   override val componentClass = Example::class.java
 *
 *   override suspend fun invoke(
 *     instance: Example,
 *     method: String,
 *     args: Array<*>,
 *   ): Any? = when (method) {
 *     "create" -> instance.create(args[0] as String, args[1] as String)
 *     "update" -> instance.update(args[0] as String, args[1] as String)
 *     "delete" -> instance.delete()
 *     "currentState" -> instance.currentState()
 *     else -> error("Unknown method '$method' for Example")
 *   }
 * }
 * ```
 *
 * 说明：
 * - `Example` 为被处理的实体类型
 * - Invoker 负责将字符串方法名映射为实体的实际方法调用
 * - `args` 按方法参数顺序传入，需在生成代码中进行类型转换
 */
class InvokerGenerator(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) {
  fun generate(descriptors: List<ComponentDescriptor>, originatingFiles: Set<KSFile>) {
    descriptors.forEach { descriptor -> generateInvoker(descriptor, originatingFiles) }
  }

  private fun generateInvoker(descriptor: ComponentDescriptor, originatingFiles: Set<KSFile>) {
    val invokerName = "${descriptor.simpleName}Invoker"

    val componentClass = ClassName.bestGuess(descriptor.qualifiedName)

    val invokeFun =
      FunSpec.builder("invoke")
        .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
        .addParameter("instance", componentClass)
        .addParameter("method", STRING)
        .addParameter("args", ARRAY.parameterizedBy(STAR))
        .returns(ANY.copy(nullable = true))
        .addCode(buildDispatchBlock(descriptor))
        .build()

    val componentClassProperty =
      PropertySpec.builder(
          "componentClass",
          Class::class.asTypeName().parameterizedBy(componentClass),
        )
        .addModifiers(KModifier.OVERRIDE)
        .initializer("%T::class.java", componentClass)
        .build()

    val invokerType =
      TypeSpec.classBuilder(invokerName)
        .addModifiers(KModifier.PUBLIC)
        .addAnnotation(Singleton::class)
        .primaryConstructor(FunSpec.constructorBuilder().addAnnotation(Inject::class).build())
        .addSuperinterface(ComponentInvoker::class.asClassName().parameterizedBy(componentClass))
        .addProperty(componentClassProperty)
        .addFunction(invokeFun)
        .build()

    FileSpec.builder(CODEGEN_PACKAGE, invokerName)
      .addType(invokerType)
      .build()
      .writeTo(codeGenerator, Dependencies(aggregating = false, *originatingFiles.toTypedArray()))
  }

  private fun buildDispatchBlock(descriptor: ComponentDescriptor): CodeBlock {
    val block = CodeBlock.builder()

    // 使用 beginControlFlow 自动处理 { 和 缩进
    block.beginControlFlow("return when (method)")

    descriptor.methods.forEach { method ->
      // 生成分支： "method" -> instance.method(args[0] as Type)
      block.add("%S -> ", method.name)
      block.add("instance.%L(", method.name)

      method.parameters.forEachIndexed { index, param ->
        if (index > 0) block.add(", ")
        block.add("args[%L] as %T", index, param.type)
      }
      block.add(")\n")
    }

    // 处理 $method：在生成的代码中显示 $method 变量
    // 这里使用 %P 配合 \$ 确保生成的 .kt 文件里保留 $ 符号
    block.addStatement(
      "else -> error(%P)",
      "Unknown method '\$method' for ${descriptor.simpleName}",
    )

    block.endControlFlow() // 自动处理 }

    return block.build()
  }
}
