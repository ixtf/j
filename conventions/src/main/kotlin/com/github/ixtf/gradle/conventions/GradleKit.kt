package com.github.ixtf.gradle.conventions

import java.io.File
import java.time.Instant
import java.util.*
import org.gradle.api.Project
import org.gradle.api.provider.Provider

@Suppress("UNCHECKED_CAST")
internal val Project.scmProvider: Provider<Map<String, String>>
  get() {
    val cacheKey = "$group.scm.provider"
    val rootExtras = rootProject.extensions.extraProperties
    if (!rootExtras.has(cacheKey)) rootExtras.set(cacheKey, createScmProvider())
    return rootExtras.get(cacheKey) as Provider<Map<String, String>>
  }

private fun Project.createScmProvider() = provider {
  // 环境检测
  val currentInstant = Instant.now().toString()
  if (providers.environmentVariable("CI").orNull == "true") {
    mapOf(
      "branch" to providers.environmentVariable("CI_COMMIT_REF_NAME").getOrElse("unknown"),
      "commitId" to providers.environmentVariable("CI_COMMIT_SHA").getOrElse("unknown"),
      "commitTime" to providers.environmentVariable("CI_COMMIT_TIMESTAMP").getOrElse(currentInstant),
    )
  } else {
    // 使用 Gradle 原生命令获取 Git 信息
    fun gitCommand(vararg args: Any) = runCatching {
      providers.exec { commandLine(*args) }.standardOutput.asText.get().trim()
    }
    mapOf(
      "branch" to gitCommand("git", "rev-parse", "--abbrev-ref", "HEAD").getOrDefault("local"),
      "commitId" to gitCommand("git", "rev-parse", "HEAD").getOrDefault("dev"),
      "commitTime" to gitCommand("git", "log", "-1", "--format=%cI").getOrDefault(currentInstant),
    )
  }
}

@Suppress("UNCHECKED_CAST")
internal fun Project.gitRootDirOrNull(): File? =
  runCatching {
      providers
        .exec {
          // workingDir 设为项目根目录，确保 git 命令能跑
          workingDir(rootProject.rootDir)
          commandLine("git", "rev-parse", "--show-toplevel")
        }
        .standardOutput
        .asText
        .get()
        .trim()
    }
    .getOrNull()
    ?.takeIf { it.isNotBlank() }
    ?.let { File(it) }
    ?.takeIf { it.exists() && it.isDirectory }

internal val defaultJvmFlags: Collection<String>
  get() =
    listOf(
      "--add-modules",
      "java.se",
      "--add-exports",
      "java.base/jdk.internal.ref=ALL-UNNAMED",
      "--add-opens",
      "java.base/java.lang=ALL-UNNAMED",
      "--add-opens",
      "java.base/java.lang.invoke=ALL-UNNAMED",
      "--add-opens",
      "java.base/java.nio=ALL-UNNAMED",
      "--add-opens",
      "java.base/sun.nio.ch=ALL-UNNAMED",
      "--add-opens",
      "java.management/sun.management=ALL-UNNAMED",
      "--add-opens",
      "jdk.management/com.sun.management.internal=ALL-UNNAMED",
      "-XX:+UseContainerSupport",
      "-Dio.netty.tryReflectionSetAccessible=true",
    )
