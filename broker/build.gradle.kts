plugins {
  id("convention-kotlin-jvm")
  id("convention-spotless")
  id("convention-maven-publish")
}

dependencies {
  api(project(":vertx"))
  api("io.vertx:vertx-auth-jwt")

  api("io.rsocket:rsocket-core")
  api("io.rsocket:rsocket-transport-netty")
  api("com.github.ben-manes.caffeine:caffeine")
  api("io.cloudevents:cloudevents-core")
}
