plugins { alias(libs.plugins.jvm) }

dependencies {
  api(platform(libs.bom))
  api(project(":core"))
  api("com.google.inject:guice")
  api("io.smallrye.config:smallrye-config")
  api("jakarta.annotation:jakarta.annotation-api")
}
