plugins { id("convention-maven-publish") }

dependencies {
  compileOnly(libs.ksp.api)
  compileOnly(project(":cqrs"))
  compileOnly(project(":broker"))

  implementation(libs.kotlinpoet)
  implementation(libs.kotlinpoet.ksp)

  testImplementation(kotlin("test"))

  compileOnly(kotlin("compiler"))
}
