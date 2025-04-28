plugins {
  `maven-publish`
  alias(libs.plugins.jvm)
}

dependencies {
  api(platform(libs.bom))
  api("cn.hutool:hutool-log")
  api("cn.hutool:hutool-crypto")
  api("cn.hutool:hutool-system")
  api("cn.hutool:hutool-setting")
  api("cn.hutool:hutool-jwt")
  api("ch.qos.logback:logback-classic")
  api("org.hibernate.validator:hibernate-validator")
  api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  api("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
  api("com.fasterxml.jackson.datatype:jackson-datatype-guava")
  api("com.fasterxml.jackson.module:jackson-module-parameter-names")
  api("com.fasterxml.jackson.module:jackson-module-kotlin")
  api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  api("com.fasterxml.jackson.dataformat:jackson-dataformat-toml")
  api("io.cloudevents:cloudevents-protobuf")
  api("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
  api("io.projectreactor.kotlin:reactor-kotlin-extensions")

  testImplementation(platform(libs.junit.bom))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation(libs.kotlin.test)
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  testImplementation("io.projectreactor:reactor-core")
  testImplementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

  testImplementation("org.bouncycastle:bcprov-jdk18on")
  testImplementation("org.seleniumhq.selenium:selenium-java:4.18.1")
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
