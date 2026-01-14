plugins {
  id("convention-kotlin-jvm")
  id("convention-spotless")
  id("convention-maven-publish")
}

dependencies {
  api(project(":core"))
  api("io.vertx:vertx-web")
  api("io.vertx:vertx-web-client")
  api("io.vertx:vertx-lang-kotlin")
  api("io.vertx:vertx-lang-kotlin-coroutines")

  api("io.vertx:vertx-health-check")
  api("io.vertx:vertx-micrometer-metrics")
  api("io.vertx:vertx-opentelemetry")
  api("io.opentelemetry.semconv:opentelemetry-semconv:1.37.0")
  api("io.opentelemetry:opentelemetry-sdk")
  api("io.opentelemetry:opentelemetry-exporter-otlp")
  api("io.opentelemetry:opentelemetry-exporter-logging")
  api("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure")
  api("io.micrometer:micrometer-registry-prometheus")
  api("io.micrometer:micrometer-registry-otlp")

  api("io.vertx:vertx-reactive-streams")
  api("io.projectreactor:reactor-core")
  api("io.projectreactor.kotlin:reactor-kotlin-extensions")
  api("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
  api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

  testImplementation("net.openhft:chronicle-queue:5.27ea11")
}
