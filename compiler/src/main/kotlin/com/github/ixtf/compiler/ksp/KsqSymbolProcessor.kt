package com.github.ixtf.compiler.ksp

import com.github.ixtf.compiler.ksp.generator.CqrsModuleGenerator
import com.github.ixtf.compiler.ksp.generator.FactoryGenerator
import com.github.ixtf.compiler.ksp.generator.InvokerGenerator
import com.github.ixtf.cqrs.eventsourcedentity.EventSourcedEntity
import com.github.ixtf.cqrs.keyvalueentity.KeyValueEntity
import com.github.ixtf.cqrs.workflow.Workflow
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate

class KsqSymbolProcessor(
  private val codeGenerator: CodeGenerator,
  private val logger: KSPLogger,
  options: Map<String, String>,
) : SymbolProcessor {
  private val baseFactoryClassList =
    listOf(EventSourcedEntity::class, KeyValueEntity::class, Workflow::class)
  private val enabled = options["cqrs.enabled"]?.toBoolean() ?: true
  private val mode = options["cqrs.mode"] ?: "scan" // scan | annotation
  private val excludedPackages =
    options["cqrs.exclude.packages"]?.split(",")?.map { it.trim() } ?: emptyList()

  private var generated = false

  override fun process(resolver: Resolver): List<KSAnnotated> {
    if (generated) {
      return emptyList()
    }

    if (!enabled) {
      logger.warn("KSP CQRS disabled by option")
      return emptyList()
    }

    val symbols =
      resolver
        .getAllFiles()
        .flatMap { it.declarations }
        .filterIsInstance<KSClassDeclaration>()
        .filter { it.validate() }
        .filter { it.isConcreteComponent() }
        .toList()
    if (symbols.isEmpty()) return emptyList()
    val descriptors = symbols.mapNotNull { it.toDescriptor() }
    if (descriptors.isEmpty()) return emptyList()
    // Collect originating files for incremental processing
    val originatingFiles = descriptors.mapNotNull { it.originatingFile }.toSet()

    CqrsModuleGenerator(codeGenerator, logger).generate(descriptors, originatingFiles)
    FactoryGenerator(codeGenerator, logger).generate(descriptors, originatingFiles)
    InvokerGenerator(codeGenerator, logger).generate(descriptors, originatingFiles)

    generated = true
    return emptyList()
  }
}
