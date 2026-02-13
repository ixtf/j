import java.time.Instant
import java.util.*
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

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

internal val Project.versionCatalog: VersionCatalog
  get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.getVersionByName(name: String): String =
  versionCatalog
    .findVersion(name)
    .map { it.requiredVersion }
    .orElseThrow { error("Could not find a version for `$name`") }

internal fun Project.getLibraryByName(name: String): Provider<MinimalExternalModuleDependency> =
  versionCatalog.findLibrary(name).orElseThrow { error("Could not find a library for `$name`") }

internal fun Project.getPluginIdByName(name: String): String =
  versionCatalog
    .findPlugin(name)
    .flatMap { Optional.ofNullable(it.orNull) }
    .map { it.pluginId }
    .orElseThrow { error("Could not find plugin id for `$name`") }

internal fun Project.gradleProperty(name: String): String {
  val provider = providers.gradleProperty(name)
  return if (provider.isPresent) {
    provider.get()
  } else {
    error("Could not find a gradleProperty for `$name`")
  }
}
