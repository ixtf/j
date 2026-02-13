package com.github.ixtf.gradle.conventions

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
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

@Suppress("unused")
class KotlinJvmConventionPlugin : Plugin<Project> {
  override fun apply(target: Project): Unit =
    with(target) {
      pluginManager.apply("com.github.ixtf.gradle.convention-spotless")
      pluginManager.apply("org.jetbrains.kotlin.jvm")
      pluginManager.apply("java-library")

      // 显式告诉 Gradle：这个项目如果要用 Kotlin，必须是这个版本
      configurations.all {
        resolutionStrategy.eachDependency {
          if (requested.group == "org.jetbrains.kotlin") {
            useVersion(BuildConfig.KOTLIN)
            because("SDK 强制统一使用指定的 Kotlin 版本以确保稳定性")
          }
        }
      }

      val javaVersion: JavaLanguageVersion = JavaLanguageVersion.of(BuildConfig.JAVA)
      extensions.configure<JavaPluginExtension> {
        toolchain { languageVersion = javaVersion }
        withJavadocJar()
        withSourcesJar()
        tasks.withType<JavaCompile> {
          options.release = javaVersion.asInt()
          options.encoding = "UTF-8"
          // 自动生成代码无需警告
          options.compilerArgs.addAll(listOf("-proc:full", "-Xlint:none", "-nowarn"))
        }
      }
      extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(javaVersion.asInt())
        compilerOptions {
          apiVersion = KotlinVersion.fromVersion(BuildConfig.KOTLIN.substringBeforeLast("."))
          languageVersion = apiVersion
          // 确保生成的字节码完美支持 Java 的特性
          jvmTarget = JvmTarget.fromTarget(BuildConfig.JAVA)
          // 开启 K2 编译器的性能优化
          freeCompilerArgs.add("-Xjdk-release=${BuildConfig.JAVA}")

          freeCompilerArgs.add("-Xjsr305=strict")
          freeCompilerArgs.add("-Xemit-jvm-type-annotations")
          freeCompilerArgs.add("-Xannotation-default-target=param-property")
          // 自动生成代码无需警告
          freeCompilerArgs.add("-Xsuppress-version-warnings")
          // 建议开启：优化接口默认方法的生成，对 Java 25 非常友好
          freeCompilerArgs.add("-jvm-default=no-compatibility")
          // freeCompilerArgs.add("-opt-in=kotlin.ExperimentalStdlibApi")
          // freeCompilerArgs.add("-Xuse-experimental=kotlin.Experimental")
          // freeCompilerArgs.add("-Xannotation-target-all")
        }
      }

      dependencies {
        add("api", platform("com.gitee.ixtf:bom:${BuildConfig.BOM}"))

        add("testImplementation", platform("org.junit:junit-bom:${BuildConfig.JUNIT}"))
        add("testImplementation", "org.junit.jupiter:junit-jupiter")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
        add("testImplementation", "org.jetbrains.kotlin:kotlin-test-junit")
        add("testImplementation", "org.jetbrains.kotlinx:kotlinx-coroutines-test")
      }

      tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        failOnNoDiscoveredTests = false
        testLogging { events("passed", "skipped", "failed") }
      }
    }
}
