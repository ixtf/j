plugins {
  `kotlin-dsl`
  alias(libs.plugins.spotless)
}

dependencies {
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.spotless.gradle.plugin)
}

spotless {
  kotlin {
    target("**/java/**/*.kt", "**/kotlin/**/*.kt")
    targetExclude("**/generated/**", "**/generated_tests/**", "**/generated-sources/**")
    ktfmt()
    trimTrailingWhitespace()
    endWithNewline()
  }
  kotlinGradle {
    target("*.gradle.kts", "**/java/**/*.gradle.kts", "**/kotlin/**/*.gradle.kts")
    targetExclude("build/**/*.kts")
    ktfmt()
    trimTrailingWhitespace()
    endWithNewline()
  }
}
