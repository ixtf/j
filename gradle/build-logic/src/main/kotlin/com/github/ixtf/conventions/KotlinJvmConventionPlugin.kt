package com.github.ixtf.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class KotlinJvmConventionPlugin : Plugin<Project> {
  override fun apply(target: Project): Unit =
    with(target) {
      pluginManager.apply("org.jetbrains.kotlin.jvm")
      pluginManager.apply("java-library")

      group = "com.github.ixtf.j"
      version = "1.0.0"

      extensions.configure<JavaPluginExtension> {
        // JavaVersion.VERSION_25.name
        toolchain { languageVersion.assign(JavaLanguageVersion.of(25)) }
        withJavadocJar()
        withSourcesJar()
        tasks.withType<JavaCompile> {
          options.release.set(25)
          options.encoding = "UTF-8"
          // 自动生成代码无需警告
          options.compilerArgs.addAll(listOf("-Xlint:none", "-nowarn"))
        }
      }
      extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(25)
        compilerOptions {
          freeCompilerArgs.add("-Xjsr305=strict")
          freeCompilerArgs.add("-Xemit-jvm-type-annotations")
          freeCompilerArgs.add("-Xannotation-default-target=param-property")
          // 自动生成代码无需警告
          freeCompilerArgs.add("-Xsuppress-version-warnings")
          // 建议开启：优化接口默认方法的生成，对 Java 25 非常友好
          freeCompilerArgs.add("-Xjvm-default=all")
          // freeCompilerArgs.add("-opt-in=kotlin.ExperimentalStdlibApi")
          // freeCompilerArgs.add("-Xuse-experimental=kotlin.Experimental")
          // freeCompilerArgs.add("-Xannotation-target-all")
        }
      }

      dependencies {
        add("api", platform(getLibraryByName("bom")))

        add("testImplementation", platform(getLibraryByName("junit.bom")))
        add("testImplementation", "org.junit.jupiter:junit-jupiter")
        add("testImplementation", getLibraryByName("kotlin.test"))
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
      }

      tasks.withType<Test>().configureEach {
        jvmArgs(
          "--add-opens",
          "java.base/java.lang=ALL-UNNAMED",
          "--enable-native-access=ALL-UNNAMED",
        )
        useJUnitPlatform()
        failOnNoDiscoveredTests = false
        testLogging { events("passed", "skipped", "failed") }
      }
    }
}
