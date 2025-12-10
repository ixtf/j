plugins {
  id("convention-kotlin-jvm")
  id("convention-spotless")
  id("convention-maven-publish")
}

dependencies {
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

  testImplementation("io.avaje:avaje-validator")
  testImplementation("org.bouncycastle:bcprov-jdk18on")
  testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

  testImplementation("io.lindstrom:m3u8-parser:0.30")
  testImplementation("io.vertx:vertx-web")
  testImplementation("io.vertx:vertx-web-client")
  testImplementation("io.vertx:vertx-lang-kotlin")
  testImplementation("io.vertx:vertx-lang-kotlin-coroutines")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
}
