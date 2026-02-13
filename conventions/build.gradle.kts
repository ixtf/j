plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  alias(libs.plugins.buildconfig)
  id("convention-maven-publish")
}

dependencies {
  implementation(libs.spotless.gradlePlugin)
  implementation(libs.kotlin.gradlePlugin)
  implementation(libs.shadow.gradlePlugin)
  implementation(libs.ksp.gradlePlugin)
}

gradlePlugin {
  plugins {
    //    register("SettingsConventionPlugin") {
    //      id = "com.github.ixtf.gradle.convention-settings"
    //      implementationClass = "com.github.ixtf.gradle.conventions.SettingsConventionPlugin"
    //    }
    register("SpotlessConventionPlugin") {
      id = "com.github.ixtf.gradle.convention-spotless"
      implementationClass = "com.github.ixtf.gradle.conventions.SpotlessConventionPlugin"
    }
    register("KotlinJvmConventionPlugin") {
      id = "com.github.ixtf.gradle.convention-kotlin-jvm"
      implementationClass = "com.github.ixtf.gradle.conventions.KotlinJvmConventionPlugin"
    }
    register("MavenPublishConventionPlugin") {
      id = "com.github.ixtf.gradle.convention-maven-publish"
      implementationClass = "com.github.ixtf.gradle.conventions.MavenPublishConventionPlugin"
    }
    register("KspConventionPlugin") {
      id = "com.github.ixtf.gradle.convention-ksp"
      implementationClass = "com.github.ixtf.gradle.conventions.KspConventionPlugin"
    }
    register("ShadowConventionPlugin") {
      id = "com.github.ixtf.gradle.convention-shadow"
      implementationClass = "com.github.ixtf.gradle.conventions.ShadowConventionPlugin"
    }
  }
}

buildConfig {
  packageName("${project.group}.gradle.${project.name}")
  useKotlinOutput { internalVisibility = false }

  buildConfigField("JAVA", "${libs.versions.java.get()}")
  buildConfigField("KOTLIN", "${libs.versions.kotlin.get()}")
  buildConfigField("BOM", "${libs.versions.bom.get()}")
  buildConfigField("VERSION", "${project.version}")
  buildConfigField("JUNIT", "${libs.versions.junit.get()}")
  buildConfigField("KSP", "${libs.versions.ksp.get()}")
  buildConfigField("DAGGER", "${libs.versions.dagger.get()}")
}
