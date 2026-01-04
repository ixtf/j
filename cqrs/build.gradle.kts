plugins {
  id("convention-kotlin-jvm")
  id("convention-spotless")
  id("convention-maven-publish")
}

dependencies {
  api(project(":vertx"))

  api("com.github.ben-manes.caffeine:caffeine")
  api(libs.dagger)

  testImplementation("io.akka:akka-javasdk")
}
