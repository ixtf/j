plugins { alias(libs.plugins.jvm) }

dependencies {
  api(platform(libs.bom))
  api(project(":core"))
  api("org.apache.poi:poi")
  api("org.apache.poi:poi-ooxml")
  api("org.apache.poi:poi-scratchpad")
}
