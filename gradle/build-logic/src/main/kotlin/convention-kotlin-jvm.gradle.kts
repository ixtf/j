plugins {
  // `java-gradle-plugin`
  // alias(libs.plugins.kotlin.jvm)
  id("org.jetbrains.kotlin.jvm")
  //id("org.jetbrains.kotlin.kapt")
  //id("com.google.devtools.ksp")
}

afterEvaluate {
  // 统一配置所有 Kotlin 库
  group = "com.github.ixtf.j"
  version = "1.0.0"

  java {
    toolchain { languageVersion = JavaLanguageVersion.of(25) }
    withJavadocJar()
    withSourcesJar()
  }
  tasks.withType<JavaCompile> {
    options.compilerArgs.add("-proc:full")
  }

  kotlin {
    jvmToolchain(25)
    compilerOptions {
      freeCompilerArgs.add("-Xjsr305=strict")
      freeCompilerArgs.add("-Xemit-jvm-type-annotations")
      freeCompilerArgs.add("-Xannotation-default-target=param-property")
      // freeCompilerArgs.add("-opt-in=kotlin.ExperimentalStdlibApi")
      // freeCompilerArgs.add("-Xuse-experimental=kotlin.Experimental")
      // freeCompilerArgs.add("-Xannotation-target-all")
    }
  }

//  kapt {
//    // 确保生成代码的路径被 IDE 识别
//    useBuildCache = true
//    // 如果有多个注解处理器，确保它们都能正常工作
//    includeCompileClasspath = false
//  }

  dependencies {
    val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

    api(platform(libs.findLibrary("bom").get()))

    testImplementation(platform(libs.findLibrary("junit.bom").get()))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.findLibrary("kotlin.test").get())
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  }

  tasks.test {
    useJUnitPlatform()
    testLogging { events("passed", "skipped", "failed") }
    failOnNoDiscoveredTests = false
  }
}
