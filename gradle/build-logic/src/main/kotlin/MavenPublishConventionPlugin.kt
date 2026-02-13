import java.time.Instant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.authentication.http.HttpHeaderAuthentication
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.attributes
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType

@Suppress("unused")
class MavenPublishConventionPlugin : Plugin<Project> {
  override fun apply(target: Project): Unit =
    with(target) {
      pluginManager.apply("convention-kotlin-jvm")
      pluginManager.apply("maven-publish")

      providers
        .environmentVariable("CI_COMMIT_TAG")
        .orNull
        ?.takeIf { it.isNotBlank() }
        ?.let { version = it.removePrefix("v") }

      tasks.withType<Jar> {
        manifest {
          attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Created-By" to
              "${System.getProperty("java.version")} (${System.getProperty("java.vendor")})",
            "Build-Timestamp" to Instant.now().toString(),
            "SCM-Branch" to scmProvider.map { it["branch"] ?: "unknown" },
            "SCM-Commit-id" to scmProvider.map { it["commitId"] ?: "unknown" },
            "SCM-Commit-id-abbrev" to scmProvider.map { it["commitId"]?.take(8) ?: "unknown" },
            "SCM-Commit-Time" to scmProvider.map { it["commitTime"] ?: Instant.now().toString() },
          )
        }
      }

      extensions.configure<PublishingExtension> {
        repositories {
          maven {
            name = "GitHubPackages"
            // 替换为你的 GitHub 用户名和仓库名
            url = uri("https://maven.pkg.github.com/ixtf/gradle")

            credentials(HttpHeaderCredentials::class) {
              // GitHub Actions 环境下自动获取 GITHUB_TOKEN
              // 本地开发需要手动设置环境变量 GITHUB_TOKEN
              name = "Authorization"
              value = "Bearer ${providers.environmentVariable("GITHUB_TOKEN").getOrElse("")}"
            }
            authentication { create<HttpHeaderAuthentication>("header") }
          }
        }
        publications {
          create<MavenPublication>("mavenJava") {
            from(components["java"])
            versionMapping {
              usage("java-api") { fromResolutionOf("runtimeClasspath") }
              usage("java-runtime") { fromResolutionResult() }
            }
            pom {
              name = project.name
              description = project.description ?: "Medipath: ${project.name}"
              scm {
                url = "https://github.com/ixtf"
                connection = "scm:git:git://github.com/ixtf"
                tag = scmProvider.map { it["commitId"]?.take(8) ?: "unknown" }
              }
            }
          }
        }
      }
    }
}
