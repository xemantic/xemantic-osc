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

  val hostOs = System.getProperty("os.name")
  val isMingwX64 = hostOs.startsWith("Windows")
  @Suppress("UNUSED_VARIABLE")
  val nativeTarget = when {
    hostOs == "Mac OS X" -> macosX64()
    hostOs == "Linux" -> linuxX64()
    isMingwX64 -> mingwX64()
    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
  }

  applyDefaultHierarchyTemplate()

  sourceSets {

    all {
      languageSettings {
        languageVersion = libs.versions.kotlinLanguageVersion.get()
        apiVersion = libs.versions.kotlinLanguageVersion.get()
        progressiveMode = true
        optIn("kotlin.ExperimentalStdlibApi")
        optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        optIn("kotlinx.coroutines.DelicateCoroutinesApi")
      }
    }

    val jvmAndNativeMain by creating {
      dependsOn(commonMain.get())
      dependencies {
        api(project(":xemantic-osc-api"))
        api(libs.ktor.network)
        implementation(libs.kotlin.logging)
      }
    }

    val jvmAndNativeTest by creating {
      dependsOn(commonTest.get())
      dependencies {
        implementation(project(":xemantic-osc-test"))
        implementation(project(":xemantic-osc-api"))
        implementation(libs.kotlin.logging)
        implementation(libs.kotlin.test)
        implementation(libs.kotlin.coroutines.test)
        implementation(libs.ktor.network)
        implementation(libs.kotest.assertions.core)
      }
    }

    jvmMain {
      dependsOn(jvmAndNativeMain)
    }

    nativeMain {
      dependsOn(jvmAndNativeMain)
    }

    jvmTest {
      dependsOn(jvmAndNativeTest)
      dependencies {
        runtimeOnly(libs.log4j.slf4j2)
        runtimeOnly(libs.log4j.core)
        runtimeOnly(libs.jackson.databind)
        runtimeOnly(libs.jackson.json)
      }
    }

    nativeTest {
      dependsOn(jvmAndNativeTest)
    }

  }

}
