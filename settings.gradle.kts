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
    maven("https://repo.akka.io/CLDJGqcFkY_87rUicOqyzLdS-W80gXkygSnrkmyvF-WNVdyF/secure")
  }
}

rootProject.name = "j"

include(":core")

include(":vertx")

include(":cqrs")

include(":broker")

include(":poi")

include(":compiler:gradle-plugin")

include(":compiler:ksp-processor")
