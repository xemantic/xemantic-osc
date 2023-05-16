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

kotlin {

  jvm {
    testRuns["test"].executionTask.configure {
      useJUnitPlatform()
    }
  }

//  val hostOs = System.getProperty("os.name")
//  val isMingwX64 = hostOs.startsWith("Windows")
//  val nativeTarget = when {
//    hostOs == "Mac OS X" -> macosX64("native")
//    hostOs == "Linux" -> linuxX64("native")
//    isMingwX64 -> mingwX64("native")
//    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
//  }

  sourceSets {

    val commonMain by getting {
      dependencies {
        implementation(project(":xemantic-osc-api"))
        implementation(libs.kotlin.logging)
      }
    }

    val jvmMain by getting {
      dependencies {
        implementation(project(":xemantic-osc-udp"))
        runtimeOnly(libs.slf4j.simple)
        //implementation(libs.kotlin.coroutines)
      }
    }

//    val linuxX64Main by getting {
//      dependencies {
//        implementation(project(":xemantic-osc-udp"))
//        //implementation(libs.kotlin.coroutines)
//      }
//    }

    /*
    val jvmAndNativeMain by getting {
      dependsOn(nativeMain)
      dependencies {
        implementation(project(":xemantic-osc-udp"))
      }
    }

     */
  }

}
