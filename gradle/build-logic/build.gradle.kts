plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  alias(libs.plugins.spotless)
}

kotlin { jvmToolchain(25) }

dependencies {
  implementation(libs.kotlin.gradlePlugin)
  implementation(libs.spotless.gradlePlugin)
  implementation(libs.ksp.gradlePlugin)
}

gradlePlugin {
  plugins {
    register("KotlinJvmConventionPlugin") {
      id = "convention-kotlin-jvm"
      implementationClass = "com.github.ixtf.conventions.KotlinJvmConventionPlugin"
    }
    register("SpotlessConventionPlugin") {
      id = "convention-spotless"
      implementationClass = "com.github.ixtf.conventions.SpotlessConventionPlugin"
    }
    register("MavenPublishConventionPlugin") {
      id = "convention-maven-publish"
      implementationClass = "com.github.ixtf.conventions.MavenPublishConventionPlugin"
    }
    register("KspConventionPlugin") {
      id = "convention-ksp"
      implementationClass = "com.github.ixtf.conventions.KspConventionPlugin"
    }
  }
}

spotless {
  kotlin {
    target("**/*.kt")
    targetExclude(
      "**/build/generated/**",
      "**/build/generated-sources/**",
      "**/generated/**",
      "bin/**",
    )
    ktfmt()
    // 增加跳过标记，允许在源码中使用 // spotless:off 跳过特定复杂的代码块
    toggleOffOn()
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
