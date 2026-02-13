pluginManagement {
  includeBuild("gradle/build-logic")

  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
  }
}

rootProject.name = "j"

include(":conventions")
include(":compiler")

include(":core")
include(":vertx")
include(":cqrs")
include(":broker")
include(":poi")
