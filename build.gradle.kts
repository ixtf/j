plugins {
  alias(libs.plugins.jvm)
  alias(libs.plugins.spotless)
}

allprojects {
  group = "com.github.ixtf.j"
  version = "1.0.0"

  apply(plugin = rootProject.libs.plugins.spotless.get().pluginId)
  configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    java {
      target("src/**/*.java")
      targetExclude("**/generated/**", "**/generated_tests/**")
      // googleJavaFormat()
      palantirJavaFormat().style("GOOGLE")
      formatAnnotations()
      trimTrailingWhitespace()
      endWithNewline()
    }
    kotlin {
      target("src/**/*.kt")
      targetExclude("**/generated/**", "**/generated_tests/**")
      ktfmt()
      trimTrailingWhitespace()
      endWithNewline()
    }
    kotlinGradle {
      target("*.gradle.kts", "additionalScripts/*.gradle.kts")
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
}

subprojects {
  apply(plugin = "maven-publish")
  apply(plugin = rootProject.libs.plugins.jvm.get().pluginId)

  java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
    withJavadocJar()
    withSourcesJar()
  }

  kotlin {
    jvmToolchain(21)
    compilerOptions {
      freeCompilerArgs.add("-Xjsr305=strict")
      freeCompilerArgs.add("-Xemit-jvm-type-annotations")
    }
  }

  configure<PublishingExtension> {
    publications {
      create<MavenPublication>("mavenJava") {
        from(components["java"])
        versionMapping {
          usage("java-api") { fromResolutionOf("runtimeClasspath") }
          usage("java-runtime") { fromResolutionResult() }
        }
      }
    }
  }
}

tasks {
  test { useJUnitPlatform() }
  wrapper {
    gradleVersion = "8.13"
    distributionType = Wrapper.DistributionType.ALL
  }
}
