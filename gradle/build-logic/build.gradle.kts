plugins {
  `kotlin-dsl`
  alias(libs.plugins.spotless)
}

dependencies {
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.spotless.gradle.plugin)
  implementation(libs.grgit.gradle.plugin)
}

spotless {
  kotlin {
    target("**/java/**/*.kt", "**/kotlin/**/*.kt")
    targetExclude("**/generated/**", "**/generated_tests/**")
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
