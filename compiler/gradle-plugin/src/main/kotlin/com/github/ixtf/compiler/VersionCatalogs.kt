package com.github.ixtf.compiler

import com.intellij.util.containers.orNull
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.versionCatalog: VersionCatalog
  get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.versionByName(name: String) =
  versionCatalog.findVersion(name).map { it.requiredVersion }.orNull()?.takeIf { it.isNotBlank() }
