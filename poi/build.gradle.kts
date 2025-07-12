plugins {
  id("convention-kotlin-jvm")
  id("convention-spotless")
  id("convention-maven-publish")
}

dependencies {
  api(project(":core"))
  api("org.apache.poi:poi")
  api("org.apache.poi:poi-ooxml")
  api("org.apache.poi:poi-scratchpad")
}
