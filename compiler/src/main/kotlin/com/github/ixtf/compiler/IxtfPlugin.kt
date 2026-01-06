package com.github.ixtf.compiler

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

@Suppress("unused")
class IxtfPlugin : Plugin<Project> {
  override fun apply(target: Project): Unit =
    with(target) {
      plugins.withId("org.jetbrains.kotlin.jvm") { pluginManager.apply("com.google.devtools.ksp") }

      configurations.all {
        // dagger 还不支持 kotlin 2.3.0
        resolutionStrategy { force("org.jetbrains.kotlin:kotlin-metadata-jvm:2.3.0") }
      }

      dependencies { add("ksp", "com.github.ixtf.compiler:1.0.0") }
    }
}
