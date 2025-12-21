plugins {
  id("convention-kotlin-jvm")
  id("convention-spotless")
  id("convention-maven-publish")
}

dependencies {
  api(project(":core"))
  implementation("io.avaje:avaje-inject")

  val injectV = "12.2"
  //  annotationProcessor("io.avaje:avaje-inject-generator:$injectV")
  // kapt("io.avaje:avaje-inject-generator")

  testImplementation("io.avaje:avaje-inject-test")
  // testAnnotationProcessor("io.avaje:avaje-inject-generator:$injectV")
  // kaptTest("io.avaje:avaje-inject-generator")
  ksp("io.avaje:avaje-inject-generator")
}
