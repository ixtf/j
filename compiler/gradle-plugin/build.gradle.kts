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

private val daggerVersion = libs.versions.dagger.get()

tasks.withType<Jar> { manifest { attributes("X-Dagger-Version" to daggerVersion) } }

dependencies {
  implementation(libs.ksp.gradlePlugin)
  implementation(kotlin("gradle-plugin"))
  compileOnly(kotlin("compiler"))
  //  compileOnly(kotlin("compiler-internal"))
  //  compileOnly(kotlin("compiler-fir"))

  testImplementation(kotlin("test"))
}
