plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  id("convention-kotlin-jvm")
  id("convention-spotless")
  id("convention-maven-publish")
}

gradlePlugin {
  plugins {
    register("IxtfPlugin") {
      id = "com.github.ixtf.j.compiler"
      implementationClass = "com.github.ixtf.compiler.IxtfPlugin"
    }
  }
}

dependencies {
  compileOnly(libs.ksp.gradlePlugin)
  compileOnly(kotlin("gradle-plugin"))
  compileOnly(kotlin("compiler"))
  //  compileOnly(kotlin("compiler-internal"))
  //  compileOnly(kotlin("compiler-fir"))

  testImplementation(kotlin("test"))
}
