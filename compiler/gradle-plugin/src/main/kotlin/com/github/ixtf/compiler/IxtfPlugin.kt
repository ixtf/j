package com.github.ixtf.compiler

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

@Suppress("unused")
class IxtfPlugin : Plugin<Project> {
  override fun apply(target: Project): Unit =
    with(target) {
      plugins.withId("org.jetbrains.kotlin.jvm") {
        if (!pluginManager.hasPlugin("com.google.devtools.ksp")) {
          pluginManager.apply("com.google.devtools.ksp")
        }
      }

      configurations.all {
        // dagger 还不支持 kotlin 2.3.0
        resolutionStrategy { force("org.jetbrains.kotlin:kotlin-metadata-jvm:2.3.0") }
      }

      val currentVersion = "1.0.0"
      dependencies { add("ksp", "com.github.ixtf.j:ksp-processor:${currentVersion}") }

      val daggerVersion = "2.58"
      dependencies { add("implementation", "com.google.dagger:dagger:${daggerVersion}") }
      dependencies { add("ksp", "com.google.dagger:dagger-compiler:${daggerVersion}") }
    }
}
