package com.github.ixtf.conventions

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class SpotlessConventionPlugin : Plugin<Project> {
  override fun apply(target: Project): Unit =
    with(target) {
      pluginManager.apply("com.diffplug.spotless")

      extensions.configure<SpotlessExtension> {
        java {
          target("**/*.java")
          targetExclude("**/build/**", "**/generated/**")
          googleJavaFormat()
          formatAnnotations()
          toggleOffOn()
          trimTrailingWhitespace()
          endWithNewline()
        }
        kotlin {
          target("**/*.kt")
          targetExclude("**/build/**", "**/generated/**")
          ktfmt().googleStyle()
          toggleOffOn()
          trimTrailingWhitespace()
          endWithNewline()
        }
        kotlinGradle {
          target("*.gradle.kts", "gradle/**/*.gradle.kts")
          targetExclude("**/build/**", "**/generated/**")
          ktfmt().googleStyle()
          toggleOffOn()
          trimTrailingWhitespace()
          endWithNewline()
        }
        format("styling") {
          target("**/resources/**/*.graphql", "**/resources/**/*.graphqls")
          targetExclude("**/build/**", "**/generated/**")
          prettier()
          toggleOffOn()
          trimTrailingWhitespace()
          endWithNewline()
        }
      }
    }
}
