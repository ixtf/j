package com.github.ixtf.gradle.conventions

import org.gradle.api.Plugin
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.maven

@Suppress("unused")
class SettingsConventionPlugin : Plugin<Settings> {
  override fun apply(target: Settings): Unit =
    with(target) {
      dependencyResolutionManagement {
        // 启用严格模式，确保所有依赖都有版本
        // versionCatalogs {
        //     create("libs") {
        //         from(files("../gradle/libs.versions.toml"))
        //     }
        // }

        @Suppress("UnstableApiUsage") repositories { configureCommonRepositories() }
        println("================== RepositoryHandler")
      }
    }

  /** 配置公共仓库的扩展函数 */
  private fun RepositoryHandler.configureCommonRepositories() {
    mavenLocal()
    mavenCentral()
    maven("https://plugins.gradle.org/m2")
    maven("https://jitpack.io")
    maven("https://repo.akka.io/CLDJGqcFkY_87rUicOqyzLdS-W80gXkygSnrkmyvF-WNVdyF/secure")
  }
}
