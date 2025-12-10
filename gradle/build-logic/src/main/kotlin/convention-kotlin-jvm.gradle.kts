plugins {
  // `java-gradle-plugin`
  // alias(libs.plugins.kotlin.jvm)
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.kotlin.kapt")
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

  kotlin {
    jvmToolchain(25)
    compilerOptions {
      freeCompilerArgs.add("-Xjsr305=strict")
      freeCompilerArgs.add("-Xemit-jvm-type-annotations")
      // freeCompilerArgs.add("-opt-in=kotlin.ExperimentalStdlibApi")
      // freeCompilerArgs.add("-Xuse-experimental=kotlin.Experimental")
      freeCompilerArgs.add("-Xannotation-default-target=param-property")
      // freeCompilerArgs.add("-Xannotation-target-all")
    }
  }

  dependencies {
    val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

    api(platform(libs.findLibrary("bom").get()))

    testImplementation(platform(libs.findLibrary("junit.bom").get()))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.findLibrary("kotlin.test").get())
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  }
}
