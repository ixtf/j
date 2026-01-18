package com.github.ixtf.compiler

import com.intellij.util.containers.orNull
import java.net.URI
import java.util.jar.Attributes
import java.util.jar.Manifest
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal fun loadManifest(): Attributes? {
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

internal fun Attributes.versionByName(name: String) =
  getValue(name).also { require(it.isNotBlank()) { "$name is missing in Manifest" } }

internal val Project.versionCatalog: VersionCatalog
  get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.versionByName(name: String) =
  versionCatalog.findVersion(name).map { it.requiredVersion }.orNull()?.takeIf { it.isNotBlank() }
