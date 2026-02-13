package com.github.ixtf.gradle.conventions

import com.diffplug.gradle.spotless.SpotlessExtension
import java.io.File
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

@Suppress("unused")
class SpotlessConventionPlugin : Plugin<Project> {
  override fun apply(target: Project): Unit =
    with(target) {
      pluginManager.apply("com.diffplug.spotless")

      repositories {
        mavenLocal()
        mavenCentral()
        maven("https://plugins.gradle.org/m2")
        maven("https://jitpack.io")
        maven("https://repo.akka.io/CLDJGqcFkY_87rUicOqyzLdS-W80gXkygSnrkmyvF-WNVdyF/secure")
      }

      extensions.configure<SpotlessExtension> {
        // ratchetFrom("origin/main")
        java {
          target("src/**/*.java")
          // targetExclude("**/build/**", "**/generated/**")
          googleJavaFormat()
          formatAnnotations()
          toggleOffOn()
          trimTrailingWhitespace()
          endWithNewline()
        }
        kotlin {
          target("src/**/*.kt")
          targetExclude("**/build/**", "**/generated/**")
          ktfmt().googleStyle()
          // ktlint()
          toggleOffOn()
          trimTrailingWhitespace()
          endWithNewline()
        }
        kotlinGradle {
          target("*.gradle.kts", "**/build.gradle.kts", "gradle/**/*.gradle.kts")
          targetExclude("**/build/**", "**/generated/**")
          ktfmt().googleStyle()
          toggleOffOn()
          trimTrailingWhitespace()
          endWithNewline()
        }
        format("styling") {
          target("src/main/resources/**/*.graphql", "src/main/resources/**/*.graphqls")
          targetExclude("**/build/**", "**/generated/**")
          prettier()
          toggleOffOn()
          trimTrailingWhitespace()
          endWithNewline()
        }
      }

      val installGitHooks =
        tasks.register("installGitHooks") {
          group = "verification"
          onlyIf { project == rootProject }

          doLast {
            val gitRootDir = gitRootDirOrNull()
            if (gitRootDir == null) {
              logger.warn("⚠️ 无法通过 git rev-parse 获取 Git 根目录，请确保已初始化 Git。")
              return@doLast
            }

            val javaHome = System.getProperty("java.home")
            val hookFile = File(gitRootDir, ".git/hooks/pre-commit")
            val projectDir = rootProject.rootDir.absolutePath
            val hookScript =
              $$"""
            #!/bin/bash
            # 由 Medipath Convention Plugin 自动生成

            export JAVA_HOME="$$javaHome"
            export PATH="$JAVA_HOME/bin:$PATH"
            PROJECT_DIR="$$projectDir"

            echo "--------------------------------------------------"
            echo "🚀 Spotless: 正在检查并自动格式化代码..."
            echo "📍 Project: $PROJECT_DIR"

            # 进入 Java 项目目录运行 Spotless
            cd "$PROJECT_DIR" || exit

            ./gradlew clean --no-daemon
            ./gradlew spotlessApply --no-daemon

            status=$?
            if [ $status -ne 0 ]; then
                echo "❌ 格式化失败，请检查代码语法。"
                exit 1
            fi

            git add $PROJECT_DIR
            echo "✅ 格式化完成并自动暂存。"
        """
                .trimIndent()

            if (!hookFile.exists() || hookFile.readText() != hookScript) {
              hookFile.writeText(hookScript)
              hookFile.setExecutable(true)
              logger.lifecycle("✅ 安装/更新 Git Pre-commit 钩子 JAVA_HOME: $javaHome")
            }
          }
        }

      //      afterEvaluate {
      //        tasks.named("spotlessCheck") { dependsOn(installGitHooks) }
      //        tasks.withType<JavaCompile> { dependsOn(installGitHooks) }
      //        tasks.withType<KotlinCompile> { dependsOn(installGitHooks) }
      //      }
    }
}
