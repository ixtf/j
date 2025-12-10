plugins {
  id("convention-kotlin-jvm")
  id("convention-spotless")
  id("convention-maven-publish")
}

dependencies {
  api(project(":core"))
  api("io.avaje:avaje-inject")
  api("io.avaje:avaje-validator")

  kapt("io.avaje:avaje-inject-generator")
  kapt("io.avaje:avaje-validator-generator")

  testImplementation("io.avaje:avaje-inject-test")
  testImplementation("io.avaje:avaje-validator")
  kaptTest("io.avaje:avaje-inject-generator")
  kaptTest("io.avaje:avaje-validator")
}
