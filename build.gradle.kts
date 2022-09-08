plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.dokka)
  `maven-publish`
}

group = "com.xemantic.osc"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

kotlin {

  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = libs.versions.jvmTarget.get()
    }
    testRuns["test"].executionTask.configure {
      useJUnitPlatform()
    }
  }

  sourceSets {

    val commonMain by getting {
      dependencies {
        // coroutine Flow is exposed by xemantic-state API
        api(libs.kotlin.coroutines)
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(libs.kotlin.test)
        implementation(libs.kotlin.coroutines.test)
        implementation(libs.kotest)
      }
    }

    val jvmMain by getting {
      dependencies {
        implementation(libs.java.osc)
        implementation(libs.kotlin.reflect)
        implementation(libs.kotlin.logging)
      }
      configurations {
        all {
          exclude("log4j", "log4j")
          exclude("org.slf4j", "slf4j-log4j12")
        }
      }
    }

    val jvmTest by getting

  }
}

tasks.dokkaHtml {
  dokkaSourceSets {
    register("customSourceSet") {
      sourceRoots.from(file("src/commonMain/kotlin"))
      sourceRoots.from(file("src/jvmMain/kotlin"))
    }
  }
}
