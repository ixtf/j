plugins {
  id("convention-kotlin-jvm")
  id("convention-spotless")
  id("convention-maven-publish")
}

dependencies {
  api(project(":core"))
  api(project(":inject"))

  testImplementation("io.akka:akka-javasdk")
}
