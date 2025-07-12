import java.time.Instant
import org.ajoberstar.grgit.Grgit

plugins { `maven-publish` }

// 通过 afterEvaluate，确保在项目配置完成后再应用发布逻辑
// 这样可以获取到项目定义的 group 和 version
afterEvaluate {
  // 环境检测
  val isCI = System.getenv("CI") == "true"
  val versionTag = System.getenv("CI_COMMIT_TAG")
  if (versionTag?.isNotBlank() == true) version = versionTag.removePrefix("v")
  val scmInfo by lazy {
    if (isCI) {
      mapOf(
          "branch" to System.getenv("CI_COMMIT_REF_NAME"),
          "commitId" to System.getenv("CI_COMMIT_SHA"),
          "commitTime" to System.getenv("CI_COMMIT_TIMESTAMP"),
      )
    } else {
      apply { plugin("org.ajoberstar.grgit") }

      val grgit = Grgit.open { dir = project.rootDir }
      val headCommit = grgit.head()
      mapOf(
          "branch" to grgit.branch.current().name,
          "commitId" to headCommit.id,
          "commitTime" to headCommit.dateTime.toInstant().toString(),
      )
    }
  }

  tasks.withType<Jar> {
    manifest {
      attributes(
          "Implementation-Title" to project.name,
          "Implementation-Version" to project.version,
          "Created-By" to
              "${System.getProperty("java.version")} (${System.getProperty("java.vendor")})",
          "Build-Timestamp" to Instant.now().toString(),
          "SCM-Branch" to "${scmInfo["branch"]}",
          "SCM-Commit-id" to "${scmInfo["commitId"]}",
          "SCM-Commit-id-abbrev" to "${scmInfo["commitId"]?.take(8)}",
          "SCM-Commit-Time" to "${scmInfo["commitTime"]}",
      )
    }
  }

  publishing {
    repositories {
      maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/ixtf/gpr")
        credentials {
          username = project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
          password = project.findProperty("gpr.key") as String? ?: System.getenv("GPR_KEY")
        }
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
          url = "https://github.com/ixtf/j"
          scm {
            connection = "scm:git:git://github.com/ixtf/j.git"
            developerConnection = "scm:git:ssh://github.com/ixtf/j.git"
            url = "https://github.com/ixtf/j"
            tag = scmInfo["commitId"]?.take(8)
          }
          developers {
            developer {
              id = "ixtf"
              name = "Tom King"
              email = "ixtf1984@gmail.com"
            }
          }
          //          licenses {
          //            license {
          //              name.set("The Apache License, Version 2.0")
          //              url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
          //            }
          //          }
        }
      }
    }
  }
}
