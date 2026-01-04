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
 * template:
 *
 * ```kotlin
 * @Singleton
 * interface ${name}Invoker @Inject constructor() : EventSourcedEntityInvoker<$name> {
 *   override val componentClass: Class<$name> get() = $name::class.java
 *
 *   override suspend fun invoke(
 *     instance: $name,
 *     methodName: String,
 *     args: Array<*>,
 *   ): Any? = when (methodName) {
 *     "create" -> instance.create(args[0] as String, args[1] as String)
 *     "update" -> instance.update(args[0] as String, args[1] as String)
 *     "delete" -> instance.delete().also { Unit }
 *     "currentState" -> instance.currentState()
 *     else -> error("""Unknown method '$methodName' for UserEntity""")
 *   }
 * }
 * ```
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
        .addParameter("methodName", STRING)
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
    block.beginControlFlow("return when (methodName)")

    descriptor.methods.forEach { method ->
      // 生成分支： "methodName" -> instance.methodName(args[0] as Type)
      block.add("%S -> ", method.name)
      block.add("instance.%L(", method.name)

      method.parameters.forEachIndexed { index, param ->
        if (index > 0) block.add(", ")
        block.add("args[%L] as %T", index, param.type)
      }
      block.add(")")

      // 如果方法返回 Unit，when 的这个分支会被认为是 Unit 类型
      // 如果 invoke 要求返回 Any?，Unit 会被自动装箱
      if (method.returnType == UNIT) {
        block.add(".also { Unit }")
      }

      // addStatement 会自动处理换行
      block.add("\n")
    }

    // 处理 $methodName：在生成的代码中显示 $methodName 变量
    // 这里使用 %P 配合 \$ 确保生成的 .kt 文件里保留 $ 符号
    block.addStatement(
      "else -> error(%P)",
      "Unknown method '\$methodName' for ${descriptor.simpleName}",
    )

    block.endControlFlow() // 自动处理 }

    return block.build()
  }
}
