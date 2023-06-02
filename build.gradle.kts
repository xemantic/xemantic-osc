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

import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.dokka)
  alias(libs.plugins.kotlin.multiplatform) apply false
  id("maven-publish")
  alias(libs.plugins.gradle.versions.plugin)
}

val jvmVersion = JvmTarget.fromTarget(libs.versions.jvmTarget.get())

allprojects {

  group = "com.xemantic.osc"
  version = "2.0-SNAPSHOT"

  repositories {
    mavenCentral()
    mavenLocal()
  }

  tasks {

    withType<KotlinCompile> {
      compilerOptions {
        jvmTarget.set(jvmVersion)
      }
    }

    withType<JavaCompile> {
      sourceCompatibility = jvmVersion.target
      targetCompatibility = jvmVersion.target
    }

  }

}

tasks {

  dokkaHtmlMultiModule.configure {
    outputDirectory.set(buildDir.resolve("dokkaCustomMultiModuleOutput"))
  }

  dependencyUpdates {
    gradleReleaseChannel = "current"
    rejectVersionIf {
      isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
  }

}

subprojects {

  apply {
    plugin("kotlin-multiplatform")
    plugin("maven-publish")
    plugin("org.jetbrains.dokka")
  }

  tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets {
      register("customSourceSet") {
        sourceRoots.from(file("src/commonMain/kotlin"))
        sourceRoots.from(file("src/jvmMain/kotlin"))
      }
    }
  }

//  tasks.withType<KotlinCompile>().configureEach {
//    kotlinOptions.jvmTarget = libs.versions.jvmTarget.get()
//  }

}

private val notStableKeywords = listOf("alpha", "beta", "rc")

fun isNonStable(
  version: String
): Boolean = notStableKeywords.any {
  version.lowercase().contains(it)
}
