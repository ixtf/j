import java.time.Instant
import org.ajoberstar.grgit.Grgit

plugins {
  `maven-publish`
}

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
          println(project.rootDir)
          println(grgit)
          println(grgit.branch)
          println(headCommit)

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
    publications {
      create<MavenPublication>("mavenJava") {
        from(components["java"])
        versionMapping {
          usage("java-api") { fromResolutionOf("runtimeClasspath") }
          usage("java-runtime") { fromResolutionResult() }
        }

        pom {
          name.set(project.name)
          description.set("A concise description of my library.")
          url.set("http://www.example.com")
          licenses {
            license {
              name.set("The Apache License, Version 2.0")
              url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
          }
          scm {
            connection = "scm:git:git://example.com/my-project.git"
            developerConnection = "scm:git:ssh://example.com/my-project.git"
            url = "https://example.com/my-project"
          }
          developers {
            developer {
              id = "ixtf"
              name = "Tom King"
              email = "ixtf1984@gmail.com"
            }
          }
        }
      }
    }
    repositories {
      maven {
        // 推荐将 URL 和凭证信息放在根项目的 gradle.properties 中管理
        // url = uri(project.property("publishing.repo.url").toString())
        // credentials {
        //     username = project.property("publishing.repo.username").toString()
        //     password = project.property("publishing.repo.password").toString()
        // }
        // 为演示方便，这里使用本地目录
        url = uri("${rootProject.buildDir}/repo")
      }
    }
  }
}
