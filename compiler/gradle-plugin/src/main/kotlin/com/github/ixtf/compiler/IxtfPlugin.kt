package com.github.ixtf.compiler

import java.util.jar.Manifest
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

@Suppress("unused")
class IxtfPlugin : Plugin<Project> {
  // 工具函数：读取 JAR 包中的 MANIFEST.MF
  private fun loadManifest(): java.util.jar.Attributes? =
    try {
      val resources = javaClass.classLoader.getResources("META-INF/MANIFEST.MF")
      while (resources.hasMoreElements()) {
        val url = resources.nextElement()
        url.openStream().use { stream ->
          val manifest = Manifest(stream)
          val attrs = manifest.mainAttributes
          // 确保读到的是我们自己的插件 jar (通过 Title 匹配)
          if (attrs.getValue("Implementation-Title")?.contains("gradle-plugin") == true) {
            return attrs
          }
        }
      }
      null
    } catch (e: Exception) {
      null
    }

  override fun apply(target: Project): Unit =
    with(target) {
      val manifest = requireNotNull(loadManifest()) { "Could not find MANIFEST.MF for IxtfPlugin" }
      val rootVersion =
        manifest.getValue("Implementation-Version").also { require(it.isNotBlank()) }
      val daggerVersionProvider =
        providers.provider {
          try {
            // 安全地获取目标项目的 Version Catalog (如果存在)
            extensions
              .getByType<VersionCatalogsExtension>()
              .named("libs")
              .findVersion("dagger")
              .map { it.requiredVersion }
              .get()
          } catch (e: Exception) {
            // 回退到 Manifest 中的硬编码版本
            manifest.getValue("X-Dagger-Version").also { require(it.isNotBlank()) }
          }
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
