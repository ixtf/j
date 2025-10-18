import java.time.Instant

plugins { `maven-publish` }

// 通过 afterEvaluate，确保在项目配置完成后再应用发布逻辑
// 这样可以获取到项目定义的 group 和 version
afterEvaluate {
  // 使用 Provider 延迟获取 SCM 信息
  val scmProvider =
      providers.provider {
        // 环境检测
        val isCI = System.getenv("CI") == "true"
        val versionTag = System.getenv("CI_COMMIT_TAG")
        if (versionTag?.isNotBlank() == true) version = versionTag.removePrefix("v")

        if (isCI) {
          mapOf(
              "branch" to System.getenv("CI_COMMIT_REF_NAME"),
              "commitId" to System.getenv("CI_COMMIT_SHA"),
              "commitTime" to System.getenv("CI_COMMIT_TIMESTAMP"),
          )
        } else {
          // Git 信息获取函数
          fun getGitBranch(): String =
              try {
                providers
                    .exec { commandLine("git", "rev-parse", "--abbrev-ref", "HEAD") }
                    .standardOutput
                    .asText
                    .get()
                    .trim()
              } catch (_: Exception) {
                "unknown"
              }

          fun getGitCommitHash(): String =
              try {
                providers
                    .exec { commandLine("git", "rev-parse", "HEAD") }
                    .standardOutput
                    .asText
                    .get()
                    .trim()
              } catch (_: Exception) {
                "unknown"
              }

          fun getGitCommitTime(): String =
              try {
                providers
                    .exec { commandLine("git", "log", "-1", "--format=%cI") }
                    .standardOutput
                    .asText
                    .get()
                    .trim()
              } catch (_: Exception) {
                Instant.now().toString()
              }
          // 使用 Gradle 原生命令获取 Git 信息
          val branch = getGitBranch()
          val commitId = getGitCommitHash()
          val commitTime = getGitCommitTime()
          mapOf(
              "branch" to branch,
              "commitId" to commitId,
              "commitTime" to commitTime,
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
          "SCM-Branch" to "${scmProvider.get()["branch"]}",
          "SCM-Commit-id" to "${scmProvider.get()["commitId"]}",
          "SCM-Commit-id-abbrev" to "${scmProvider.get()["commitId"]?.take(8)}",
          "SCM-Commit-Time" to "${scmProvider.get()["commitTime"]}",
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
            tag = scmProvider.get()["commitId"]?.take(8)
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
