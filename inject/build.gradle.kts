plugins {
  id("convention-kotlin-jvm")
  id("convention-spotless")
  id("convention-maven-publish")
}

val injectV = "12.1"
val validatorV = "2.15"

dependencies {
  api(project(":core"))
  implementation("io.avaje:avaje-inject")

  //  annotationProcessor("io.avaje:avaje-inject-generator:$injectV")
  //  annotationProcessor("io.avaje:avaje-validator-generator:$validatorV")
  kapt("io.avaje:avaje-inject-generator:$injectV")
  kapt("io.avaje:avaje-validator-generator:$validatorV")

  testImplementation("io.avaje:avaje-inject-test:$injectV")
  //  testAnnotationProcessor("io.avaje:avaje-inject-generator:$injectV")
  //  testAnnotationProcessor("io.avaje:avaje-validator-generator:$validatorV")
  kaptTest("io.avaje:avaje-inject-generator:$injectV")
}
