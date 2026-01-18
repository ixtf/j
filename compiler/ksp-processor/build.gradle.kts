plugins {
  id("convention-kotlin-jvm")
  id("convention-spotless")
  id("convention-maven-publish")
}

dependencies {
  implementation(project(":cqrs"))
  implementation(project(":broker"))

  implementation(libs.ksp.api)
  implementation(libs.kotlinpoet)
  implementation(libs.kotlinpoet.ksp)

  testImplementation(kotlin("test"))

  compileOnly(kotlin("compiler"))
}
