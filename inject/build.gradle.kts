plugins {
  id("convention-kotlin-jvm")
  id("convention-spotless")
  id("convention-maven-publish")
}

dependencies {
  api(project(":core"))
  api("io.avaje:avaje-inject:11.6")
  api("io.avaje:avaje-validator:2.13")

  kapt("io.avaje:avaje-inject-generator:11.6")
  kapt("io.avaje:avaje-validator-generator:2.13")
}
