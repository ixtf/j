plugins { id("convention-maven-publish") }

dependencies {
  api(project(":vertx"))
  api("io.vertx:vertx-auth-jwt")

  api("io.rsocket:rsocket-core")
  api("io.rsocket:rsocket-transport-netty")
  api("com.github.ben-manes.caffeine:caffeine")
  api("io.cloudevents:cloudevents-protobuf")

  testImplementation("io.netty:netty-transport-native-kqueue::osx-x86_64")
  testImplementation("io.netty:netty-transport-native-kqueue::osx-aarch_64")
  testImplementation("io.netty:netty-tcnative-boringssl-static::osx-x86_64")
  testImplementation("io.netty:netty-tcnative-boringssl-static::osx-aarch_64")

  testImplementation("io.aeron:aeron-client:1.50.1")
  testImplementation("io.aeron:aeron-all:1.50.1")
  testImplementation("io.aeron:aeron-all:1.50.1")
  testImplementation("org.jmdns:jmdns:3.6.3")
}

tasks.withType<JavaExec> {
  jvmArgs(
    "--add-opens",
    "java.base/jdk.internal.misc=ALL-UNNAMED",
    "--add-opens",
    "java.base/java.util.concurrent.atomic=ALL-UNNAMED",
    "--add-opens",
    "java.base/sun.nio.ch=ALL-UNNAMED",
    "--add-opens",
    "java.base/java.nio=ALL-UNNAMED",
  )
}
