plugins {
  `maven-publish`
  alias(libs.plugins.jvm)
}

dependencies {
  api(platform(libs.bom))
  api(project(":core"))
  api("org.apache.poi:poi")
  api("org.apache.poi:poi-ooxml")
  api("org.apache.poi:poi-scratchpad")
}

publishing {
  //  repositories {
  //    maven {
  //      name = "GitHubPackages"
  //      url = uri("https://maven.pkg.github.com/ixtf/gpr")
  //      credentials {
  //        username = project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
  //        password = project.findProperty("gpr.key") as String? ?: System.getenv("GPR_KEY")
  //      }
  //    }
  //  }
  publications {
    //    register<MavenPublication>("gpr") {
    //      from(components["java"])
    //    }
    create<MavenPublication>("mavenJava") {
      //      group = "com.gitee.ixtf"
      //      artifactId = "core"
      //      version = "1.0.0"
      from(components["java"])
      //      val archives by configurations
      //      setArtifacts(archives.artifacts)
      versionMapping {
        usage("java-api") { fromResolutionOf("runtimeClasspath") }
        usage("java-runtime") { fromResolutionResult() }
      }
    }
  }
}
