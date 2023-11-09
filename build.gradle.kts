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
import java.net.URI

plugins {
  alias(libs.plugins.dokka)
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.versions)
  `maven-publish`
}

val jvmVersion = JvmTarget.fromTarget(libs.versions.jvmTarget.get())

allprojects {

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
    outputDirectory.set(layout.buildDirectory.dir("dokkaCustomMultiModuleOutput"))
  }

  dependencyUpdates {

    val nonStableKeywords = listOf("alpha", "beta", "rc")

    fun isNonStable(
      version: String
    ): Boolean = nonStableKeywords.any {
      version.lowercase().contains(it)
    }

    gradleReleaseChannel = "current"
    rejectVersionIf {
      isNonStable(candidate.version) && !isNonStable(currentVersion)
    }

  }

}

subprojects {

  apply {
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

  tasks.withType<Test>().configureEach {
//    testLogging {
//      events("started", "passed", "skipped", "failed", "standardOut", "standardError")
//      exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
//      showExceptions = true
//      showStackTraces = true
//      showCauses = true
//    }
  }

  publishing {
//    publications.withType<MavenPublication> {
//
//      // Stub javadoc.jar artifact
//      //artifact(javadocJar.get())
//      artifactId = "${rootProject.name}-${project.name}"
//    }
//    publications {
//      create<MavenPublication>("xemantic-osc") {
//        from(components["kotlin"])
//        artifactId = "${rootProject.name}-${project.name}"
//      }
//    }
    repositories {
      maven {
        name = "GitHubPackages"
        url = URI("https://maven.pkg.github.com/krisenchat/krisenchat-commons")
        credentials {
          username = System.getenv("GITHUB_ACTOR")
          password = System.getenv("GITHUB_TOKEN")
        }
      }
    }
  }

//  tasks.withType<KotlinCompile>().configureEach {
//    kotlinOptions.jvmTarget = libs.versions.jvmTarget.get()
//  }

}
