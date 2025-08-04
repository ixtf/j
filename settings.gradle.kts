pluginManagement {
  includeBuild("gradle/build-logic")

  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  @Suppress("UnstableApiUsage")
  repositories {
    mavenLocal()
    mavenCentral()
    maven("https://plugins.gradle.org/m2")
    maven("https://jitpack.io")
    maven("https://repo.akka.io/maven")
  }
}

rootProject.name = "j"

include(":core")

include(":inject")

include(":poi")
