import com.diffplug.gradle.spotless.SpotlessExtension
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
          targetExclude("**/build/**", "**/generated/**")
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
    }
}
