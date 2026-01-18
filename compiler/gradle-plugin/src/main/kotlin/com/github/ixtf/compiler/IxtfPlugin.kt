package com.github.ixtf.compiler

import java.net.URI
import java.util.jar.Manifest
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

@Suppress("unused")
class IxtfPlugin : Plugin<Project> {
  private fun loadManifest(): java.util.jar.Attributes? {
    // 技巧：获取当前类在 Classloader 中的路径，精准定位其对应的 MANIFEST.MF
    val className = IxtfPlugin::class.java.simpleName + ".class"
    val classPath = IxtfPlugin::class.java.getResource(className)?.toString() ?: return null

    // 如果是 JAR 包，路径会以 jar:file: 开头
    val manifestPath =
      if (classPath.startsWith("jar")) {
        classPath.substringBeforeLast("!") + "!/META-INF/MANIFEST.MF"
      } else {
        // 本地 IDE 运行环境（build/classes 目录）
        classPath.substringBefore("com/github/ixtf/compiler") + "META-INF/MANIFEST.MF"
      }

    return try {
      URI(manifestPath).toURL().openStream().use { Manifest(it).mainAttributes }
    } catch (e: Exception) {
      null
    }
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
