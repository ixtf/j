package com.github.ixtf.compiler.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class KsqSymbolProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
    return KsqSymbolProcessor(
      codeGenerator = environment.codeGenerator,
      logger = environment.logger,
      options = environment.options,
    )
  }
}
