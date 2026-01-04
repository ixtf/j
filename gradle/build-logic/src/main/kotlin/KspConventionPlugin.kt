import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KspConventionPlugin : Plugin<Project> {
  override fun apply(target: Project): Unit =
    with(target) {
      pluginManager.apply("com.google.devtools.ksp")

      configurations.all {
        // dagger 还不支持 kotlin 2.3.0
        resolutionStrategy { force("org.jetbrains.kotlin:kotlin-metadata-jvm:2.3.0") }
      }

      dependencies {
        // 自动为所有模块加上你的 CQRS 编译器
        // add("ksp", "com.github.ixtf:cqrs-compiler:latest")
        // add("implementation", "com.github.ixtf:cqrs-core:latest")

        versionCatalog.findLibrary("dagger-compiler").ifPresent { add("ksp", it) }
      }
    }
}
