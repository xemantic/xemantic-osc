/*
 * xemantic-osc - Kotlin idiomatic and multiplatform OSC protocol support
 * Copyright (C) 2023 Kazimierz Pogoda
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
}

kotlin {

  explicitApi()

  jvm {}

  js {
    browser {}
  }

  val hostOs = System.getProperty("os.name")
  val isMingwX64 = hostOs.startsWith("Windows")
  @Suppress("UNUSED_VARIABLE")
  val nativeTarget = when {
    hostOs == "Mac OS X" -> macosX64()
    hostOs == "Linux" -> linuxX64()
    isMingwX64 -> mingwX64()
    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
  }

  sourceSets {

    all {
      languageSettings {
        languageVersion = libs.versions.kotlinLanguageVersion.get()
        apiVersion = libs.versions.kotlinLanguageVersion.get()
        progressiveMode = true
        optIn("kotlin.ExperimentalStdlibApi")
        optIn("kotlin.ExperimentalUnsignedTypes")
        optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
      }
    }

    commonMain {
      dependencies {
        api(libs.kotlin.coroutines) // Flow is exposed by xemantic-osc API
        implementation(libs.kotlin.datetime)
        implementation(project(":xemantic-osc-collections"))
        implementation(libs.ktor.io)
        implementation(libs.kotlin.logging)
      }
    }

    commonTest {
      dependencies {
        implementation(libs.kotlin.test)
        implementation(libs.kotlin.coroutines.test)
        implementation(libs.kotest.assertions.core)
      }
    }

    jvmTest  {
      dependencies {
        runtimeOnly(libs.log4j.slf4j2)
        runtimeOnly(libs.log4j.core)
        runtimeOnly(libs.jackson.databind)
        runtimeOnly(libs.jackson.json)
      }
    }

  }

}
