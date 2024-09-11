/*
 * xemantic-osc - Kotlin idiomatic and multiplatform OSC protocol support
 * Copyright (C) 2024 Kazimierz Pogoda
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

kotlin {

  explicitApi()

  jvm()
//  // iOS
//  iosX64()
//  iosArm64()
//  iosSimulatorArm64()
//
//  // Desktop
//  mingwX64()
  linuxX64()
//  linuxArm64()
//  macosX64()
//  macosArm64()
//
//  // other apple
//  watchosSimulatorArm64()
//  watchosX64()
//  watchosArm32()
//  watchosArm64()
//  tvosSimulatorArm64()
//  tvosX64()
//  tvosArm64()
//  watchosDeviceArm64()
//
//  // other android
//  androidNativeArm32()
//  androidNativeArm64()
//  androidNativeX86()
//  androidNativeX64()
//
//  // Web
  js {
    browser()
    nodejs()
  }

//  @Suppress("OPT_IN_USAGE")
//  wasmJs {
//    // To build distributions for and run tests use one or several of:
//    browser()
//    nodejs()
//    //d8()
//  }

  sourceSets {

    all {
      languageSettings {
        optIn("kotlin.ExperimentalStdlibApi")
        optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
      }
    }

    commonMain {
      dependencies {
        implementation(project(":xemantic-osc-api"))
        implementation(libs.kotlin.logging)
      }
    }

    commonTest {
      dependencies {
        implementation(project(":xemantic-osc-test"))
        implementation(libs.kotlin.test)
        implementation(libs.kotlin.coroutines.test)
        implementation(libs.kotest.assertions.core)
      }
    }

    jvmMain {
      dependencies {
        runtimeOnly(libs.log4j.slf4j2)
        runtimeOnly(libs.log4j.core)
        runtimeOnly(libs.jackson.databind)
        runtimeOnly(libs.jackson.dataformat.yaml)
      }
    }

    jvmTest {
      dependencies {
        implementation(libs.mockk)
      }
    }

  }

}
