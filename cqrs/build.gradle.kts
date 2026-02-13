plugins { id("convention-maven-publish") }

dependencies {
  api(project(":vertx"))
  api("io.kurrent:kurrentdb-client")
  api("com.github.ben-manes.caffeine:caffeine")
  api(libs.dagger)

  testImplementation("io.akka:akka-javasdk:3.5.9")
}
