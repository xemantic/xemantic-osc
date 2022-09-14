/*
 * xemantic-osc - Kotlin idiomatic and multiplatform OSC protocol support
 * Copyright (C) 2022 Kazimierz Pogoda
 *
 * This file is part of xemantic-osc.
 *
 * xemantic-osc is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * xemantic-osc is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with xemantic-osc.
 * If not, see <https://www.gnu.org/licenses/>.
 */

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.dokka)
  `maven-publish`
  alias(libs.plugins.gradle.versions.plugin)
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
        api(libs.ktor.io)
        implementation(libs.ktor.network)
        implementation(libs.kotlin.logging)
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(libs.kotlin.test)
        implementation(libs.kotlin.coroutines.test)
        implementation(libs.kotest)
      }
    }

    val jvmMain by getting

    val jvmTest by getting

  }

}

tasks.dokkaHtml {
  dokkaSourceSets {
    register("customSourceSet") {
      sourceRoots.from(file("src/commonMain/kotlin"))
    }
  }
}
