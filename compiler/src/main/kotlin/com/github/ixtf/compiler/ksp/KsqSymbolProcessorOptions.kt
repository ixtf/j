package com.github.ixtf.compiler.ksp

class KsqSymbolProcessorOptions(options: Map<String, String>) {
  val enabled = options["cqrs.enabled"]?.toBoolean() ?: true
  val mode = options["cqrs.mode"] ?: "scan" // scan | annotation
  val excludedPackages =
    options["cqrs.exclude.packages"]?.split(",")?.map { it.trim() } ?: emptyList()
}
