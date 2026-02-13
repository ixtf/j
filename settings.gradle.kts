pluginManagement {
  includeBuild("gradle/build-logic")

  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
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
