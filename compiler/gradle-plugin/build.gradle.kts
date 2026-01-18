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
      id = "com.github.ixtf.compiler"
      implementationClass = "com.github.ixtf.compiler.IxtfPlugin"
    }
  }
}

dependencies {
  implementation(libs.ksp.gradlePlugin)
  implementation(kotlin("gradle-plugin"))
  compileOnly(kotlin("compiler"))
  //  compileOnly(kotlin("compiler-internal"))
  //  compileOnly(kotlin("compiler-fir"))

  testImplementation(kotlin("test"))
}
