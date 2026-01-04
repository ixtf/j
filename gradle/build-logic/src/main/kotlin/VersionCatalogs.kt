import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

internal val Project.versionCatalog: VersionCatalog
  get() = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.getVersionByName(name: String): String {
  val version = versionCatalog.findVersion(name)
  return if (version.isPresent) {
    version.get().requiredVersion
  } else {
    error("Could not find a version for `$name`")
  }
}

internal fun Project.getLibraryByName(name: String): Provider<MinimalExternalModuleDependency> {
  val library = versionCatalog.findLibrary(name)
  return if (library.isPresent) {
    library.get()
  } else {
    error("Could not find a library for `$name`")
  }
}

internal fun Project.getPluginIdByName(name: String): String {
  val plugin = versionCatalog.findPlugin(name)
  return if (plugin.isPresent) {
    plugin.get().map { it.pluginId }.get()
  } else {
    error("Could not find plugin id for `$name`")
  }
}
