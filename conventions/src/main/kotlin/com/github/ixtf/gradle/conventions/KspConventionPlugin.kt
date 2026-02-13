package com.github.ixtf.gradle.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

@Suppress("unused")
class KspConventionPlugin : Plugin<Project> {
  override fun apply(target: Project): Unit =
    with(target) {
      pluginManager.apply("com.github.ixtf.gradle.convention-kotlin-jvm")
      pluginManager.apply("com.google.devtools.ksp")

      configurations.all {
        // dagger 还不支持 kotlin 2.3.0
        resolutionStrategy {
          force("org.jetbrains.kotlin:kotlin-metadata-jvm:${BuildConfig.KOTLIN}")
        }
      }

      dependencies {
        add("implementation", "com.github.ixtf:cqrs:${BuildConfig.DAGGER}")
        add("implementation", "com.github.ixtf:broker:${BuildConfig.DAGGER}")
        add("ksp", "com.github.ixtf:compiler:${BuildConfig.VERSION}")

        add("implementation", "com.google.dagger:dagger:${BuildConfig.DAGGER}")
        add("ksp", "com.google.dagger:dagger-compiler:${BuildConfig.DAGGER}")
      }
    }
}
