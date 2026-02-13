pluginManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  versionCatalogs { create("libs") { from(files("../libs.versions.toml")) } }

  @Suppress("UnstableApiUsage")
  repositories {
    mavenLocal()
    mavenCentral()
    maven("https://plugins.gradle.org/m2")
    maven("https://repo.akka.io/CLDJGqcFkY_87rUicOqyzLdS-W80gXkygSnrkmyvF-WNVdyF/secure")
    maven("https://jitpack.io")
  }
}

rootProject.name = "build-logic"
