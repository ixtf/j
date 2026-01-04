plugins {
  id("convention-kotlin-jvm")
  id("convention-spotless")
  id("convention-maven-publish")
}

dependencies {
  api(project(":core"))

  compileOnly(kotlin("compiler"))
  compileOnly(kotlin("compiler-internal"))
  compileOnly(kotlin("compiler-fir"))
  //  compileOnly("org.jetbrains.kotlin:kotlin-compiler-internal:2.3.0")
  //  compileOnly("org.jetbrains.kotlin:kotlin-compiler-fir:2.3.0")

  testImplementation(kotlin("test"))
}
