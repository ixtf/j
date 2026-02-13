package com.github.ixtf.gradle.conventions

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.attributes
import org.gradle.kotlin.dsl.withType

@Suppress("unused")
class ShadowConventionPlugin : Plugin<Project> {
  override fun apply(target: Project): Unit =
    with(target) {
      pluginManager.apply("com.gradleup.shadow")
      pluginManager.apply("application")

      tasks.withType<ShadowJar>().configureEach {
        // 合并 Service 文件 (比如多个库都有 META-INF/services)
        mergeServiceFiles()

        // 移除签名文件，防止合并后 JAR 无法运行
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")

        // 针对 Java 25 的优化
        manifest { attributes("Multi-Release" to true) }
      }
    }
}
