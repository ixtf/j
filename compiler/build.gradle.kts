plugins {
  id("convention-kotlin-jvm")
  id("convention-spotless")
  id("convention-maven-publish")
}

dependencies {
  implementation(project(":vertx"))
  implementation(project(":cqrs"))

  implementation(libs.ksp.api)
  implementation(libs.kotlinpoet)
  implementation(libs.kotlinpoet.ksp)

  compileOnly(kotlin("gradle-plugin"))
  compileOnly(kotlin("compiler"))
  compileOnly(kotlin("compiler-internal"))
  compileOnly(kotlin("compiler-fir"))

  testImplementation(kotlin("test"))
}
