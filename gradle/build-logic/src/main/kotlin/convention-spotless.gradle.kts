plugins { id("com.diffplug.spotless") }

spotless {
  java {
    target("**/java/**/*.java", "**/kotlin/**/*.java")
    targetExclude("**/generated/**", "**/generated_tests/**", "**/generated-sources/**")
    googleJavaFormat()
    formatAnnotations()
    trimTrailingWhitespace()
    endWithNewline()
  }
  kotlin {
    target("**/java/**/*.kt", "**/kotlin/**/*.kt")
    targetExclude("**/generated/**", "**/generated_tests/**", "**/generated-sources/**")
    ktfmt()
    trimTrailingWhitespace()
    endWithNewline()
  }
  kotlinGradle {
    target("*.gradle.kts")
    targetExclude("build/**/*.kts")
    ktfmt()
    trimTrailingWhitespace()
    endWithNewline()
  }
  format("styling") {
    target("**/resources/**/*.graphql", "**/resources/**/*.graphqls")
    prettier()
    trimTrailingWhitespace()
    endWithNewline()
  }
}
