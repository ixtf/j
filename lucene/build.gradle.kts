plugins { alias(libs.plugins.jvm) }

dependencies {
  api(platform(libs.bom))
  api(project(":core"))
}
