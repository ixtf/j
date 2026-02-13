plugins { id("convention-maven-publish") }

dependencies {
  api(project(":core"))
  api("org.apache.poi:poi")
  api("org.apache.poi:poi-ooxml")
  api("org.apache.poi:poi-scratchpad")

  testImplementation("org.jetbrains.exposed:exposed-core:1.0.0")
  testImplementation("org.jetbrains.exposed:exposed-jdbc:1.0.0")
  testImplementation("com.h2database:h2:2.4.240")
}
