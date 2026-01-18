package com.github.ixtf.compiler

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

@Suppress("unused")
class IxtfPlugin : Plugin<Project> {
  override fun apply(target: Project): Unit =
    with(target) {
      val manifest = requireNotNull(loadManifest()) { "Could not find IxtfPlugin[MANIFEST.MF]" }
      val rootVersion = manifest.versionByName("Implementation-Version")
      val daggerVersion = manifest.versionByName("X-Dagger-Version")
      val versionCatalog = versionCatalog()
      val daggerVersionProvider =
        providers.provider {
          runCatching { versionCatalog.versionByName("dagger") }.getOrElse { daggerVersion }
        }

      plugins.withId("org.jetbrains.kotlin.jvm") {
        if (!pluginManager.hasPlugin("com.google.devtools.ksp")) {
          pluginManager.apply("com.google.devtools.ksp")
        }
      }

      configurations.all {
        // dagger 还不支持 kotlin 2.3.0
        resolutionStrategy { force("org.jetbrains.kotlin:kotlin-metadata-jvm:2.3.0") }
      }

      dependencies {
        // 核心组件版本跟随插件
        add("ksp", "com.github.ixtf.j:ksp-processor:$rootVersion")
        add("api", "com.github.ixtf.j:cqrs:$rootVersion")
        add("api", "com.github.ixtf.j:broker:$rootVersion")

        // Dagger 版本延迟解析
        add("implementation", daggerVersionProvider.map { "com.google.dagger:dagger:$it" })
        add("ksp", daggerVersionProvider.map { "com.google.dagger:dagger-compiler:$it" })
      }
    }
}
