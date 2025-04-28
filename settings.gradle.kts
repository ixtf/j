pluginManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.akka.io/maven")
  }
}

rootProject.name = "j"

include("core")

include("guice")

include("rsocket")

include("lucene")

include("poi")
