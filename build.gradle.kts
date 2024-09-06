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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  `maven-publish`
  signing
  alias(libs.plugins.versions)
  alias(libs.plugins.dokka)
  alias(libs.plugins.publish)
}

val githubAccount = "xemantic"

val javaTarget = libs.versions.javaTarget.get()
val kotlinTarget = KotlinVersion.fromVersion(libs.versions.kotlinTarget.get())

val isReleaseBuild = !project.version.toString().endsWith("-SNAPSHOT")
val githubActor: String? by project
val githubToken: String? by project
val signingKey: String? by project
val signingPassword: String? by project
val sonatypeUser: String? by project
val sonatypePassword: String? by project

println("""
  Project: ${project.name}
  Version: ${project.version}
  Release: $isReleaseBuild
""".trimIndent()
)

allprojects {
  repositories {
    mavenCentral()
  }
}

tasks {

//  dokkaHtmlMultiModule.configure {
//    outputDirectory.set(layout.buildDirectory.dir("dokkaCustomMultiModuleOutput"))
//  }

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

  if (project.name == "xemantic-osc-ableton-tools") {
    apply {
      plugin("application")
      plugin("org.jetbrains.kotlin.jvm")
    }
  } else {
    apply(plugin = "org.jetbrains.kotlin.multiplatform")
  }

  apply {
    plugin("maven-publish")
    plugin("org.jetbrains.dokka")
    plugin("signing")
  }

  tasks {

    // set up according to https://jakewharton.com/gradle-toolchains-are-rarely-a-good-idea/
    withType<KotlinJvmCompile> {
      compilerOptions {
        apiVersion = kotlinTarget
        languageVersion = kotlinTarget
        jvmTarget = JvmTarget.fromTarget(javaTarget)
        freeCompilerArgs.add("-Xjdk-release=$javaTarget")
        progressiveMode = true
      }
    }

    withType<JavaCompile> {
      options.release = javaTarget.toInt()
    }

  }

//  configure<JavaPluginExtension> {
//    withJavadocJar()
//    withSourcesJar()
//  }

  configure<PublishingExtension> {
    repositories {
      if (!isReleaseBuild) {
        maven {
          name = "GitHubPackages"
          setUrl("https://maven.pkg.github.com/$githubAccount/${rootProject.name}")
          credentials {
            username = githubActor
            password = githubToken
          }
        }
      }
    }
    publications {
      create<MavenPublication>("maven") {
        from(components["kotlin"])
//        artifact(tasks.named<Jar>("javadocJar"))
//        artifact(tasks.named<Jar>("sourcesJar"))
        pom {
          name = "xemantic-kotlin-swing-dsl"
          description = "Kotlin-idiomatic and multiplatform OSC protocol support"
          url = "https://github.com/$githubAccount/${rootProject.name}"
          inceptionYear = "2020"
          organization {
            name = "Xemantic"
            url = "https://xemantic.com"
          }
          licenses {
            license {
              name = "GNU Lesser General Public License 3"
              url = "https://www.gnu.org/licenses/lgpl-3.0.en.html"
              distribution = "repo"
            }
          }
          scm {
            url = "https://github.com/$githubAccount/${rootProject.name}"
            connection = "scm:git:git:github.com/$githubAccount/${rootProject.name}.git"
            developerConnection = "scm:git:https://github.com/$githubAccount/${rootProject.name}.git"
          }
          ciManagement {
            system = "GitHub"
            url = "https://github.com/$githubAccount/${rootProject.name}/actions"
          }
          issueManagement {
            system = "GitHub"
            url = "https://github.com/$githubAccount/${rootProject.name}/issues"
          }
          developers {
            developer {
              id = "morisil"
              name = "Kazik Pogoda"
              email = "morisil@xemantic.com"
            }
          }
        }
      }
    }
  }

  if (isReleaseBuild) {
    configure<SigningExtension> {
      useInMemoryPgpKeys(
        signingKey,
        signingPassword
      )
      sign(publishing.publications["maven"])
    }
  }

  tasks {

    withType<Jar> {
      manifest {
        attributes(
          mapOf(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "Xemantic",
            "Built-By" to "Gradle ${gradle.gradleVersion}",
            "Built-Date" to LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
          )
        )
      }
      metaInf {
        from(rootProject.rootDir) {
          include("LICENSE")
        }
      }
    }

//    named<Jar>("javadocJar") {
//      from(named("dokkaJavadoc"))
//    }

  }

}

if (isReleaseBuild) {
  nexusPublishing {
    repositories {
      sonatype {  //only for users registered in Sonatype after 24 Feb 2021
        nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
        snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        username.set(sonatypeUser)
        password.set(sonatypePassword)
      }
    }
  }
}

//  tasks.withType<DokkaTask>().configureEach {
//    dokkaSourceSets {
//      register("customSourceSet") {
//        sourceRoots.from(file("src/commonMain/kotlin"))
//        sourceRoots.from(file("src/jvmMain/kotlin"))
//      }
//    }
//  }

//  tasks.withType<Test>().configureEach {
////    testLogging {
////      events("started", "passed", "skipped", "failed", "standardOut", "standardError")
////      exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
////      showExceptions = true
////      showStackTraces = true
////      showCauses = true
////    }
//  }

//  publishing {
////    publications.withType<MavenPublication> {
////
////      // Stub javadoc.jar artifact
////      //artifact(javadocJar.get())
////      artifactId = "${rootProject.name}-${project.name}"
////    }
////    publications {
////      create<MavenPublication>("xemantic-osc") {
////        from(components["kotlin"])
////        artifactId = "${rootProject.name}-${project.name}"
////      }
////    }
//    repositories {
//      maven {
//        name = "GitHubPackages"
//        url = URI("https://maven.pkg.github.com/krisenchat/krisenchat-commons")
//        credentials {
//          username = System.getenv("GITHUB_ACTOR")
//          password = System.getenv("GITHUB_TOKEN")
//        }
//      }
//    }
//  }

//  tasks.withType<KotlinCompile>().configureEach {
//    kotlinOptions.jvmTarget = libs.versions.jvmTarget.get()
//  }