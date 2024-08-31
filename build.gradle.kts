import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  val kotlinVersion = "2.0.20"

  kotlin("jvm") version kotlinVersion
  kotlin("plugin.serialization") version kotlinVersion
  `java-library`
  `maven-publish`

  id("com.diffplug.spotless") version "6.25.0"
  id("org.jetbrains.dokka") version "1.9.20"
  id("org.jetbrains.kotlinx.kover") version "0.8.3"
  id("org.jreleaser") version "1.13.1"
}

group = "com.github.ivsokol"

version = "1.0.0"

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  api("com.fasterxml.jackson.core:jackson-databind:${project.property("jacksonVersion")}")
  implementation(
      "org.jetbrains.kotlinx:kotlinx-serialization-json:${project.property("kotlinSerializationVersion")}")
  implementation("io.burt:jmespath-jackson:${project.property("jmesPathJacksonVersion")}")
  implementation("com.arakelian:java-jq:${project.property("jjqVersion")}")
  implementation(
      "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${project.property("jacksonJSR310Version")}")
  implementation("ch.qos.logback:logback-classic:${project.property("logbackVersion")}")
  implementation("com.github.fslev:json-compare:${project.property("jsonCompareVersion")}")
  implementation("io.vertx:vertx-json-schema:${project.property("vertxJsonSchemaVersion")}")
  implementation("com.aventrix.jnanoid:jnanoid:${project.property("nanoIdVersion")}")
  implementation(
      "com.github.java-json-tools:json-patch:${project.property("jsonPatchVersion")}") // 1.13

  testImplementation("io.kotest:kotest-runner-junit5:${project.property("kotestVersion")}")
  testImplementation("io.kotest:kotest-assertions-json-jvm:${project.property("kotestVersion")}")
  testImplementation("io.kotest:kotest-framework-datatest-jvm:${project.property("kotestVersion")}")
  testImplementation("io.kotest:kotest-extensions-now-jvm:${project.property("kotestVersion")}")
  testImplementation(
      "io.kotest.extensions:kotest-extensions-clock:${project.property("kotestExtClockVersion")}")
  testImplementation("io.mockk:mockk:${project.property("mockkVersion")}")
}

tasks.withType<Test>().configureEach {
  jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED")
  useJUnitPlatform()
  maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
  reports.html.required.set(false)
}

tasks.dokkaJavadoc.configure { outputDirectory.set(layout.buildDirectory.dir("docs/javadoc")) }

tasks.register<Jar>("dokkaJavadocJar") {
  dependsOn(tasks.dokkaJavadoc)
  from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
  archiveClassifier.set("javadoc")
}

project.tasks.getByName("jar").dependsOn("dokkaJavadocJar")

project.tasks.getByName("javadoc").dependsOn("dokkaJavadoc")

project.tasks.getByName("jreleaserFullRelease").dependsOn("publish")

java {
  withSourcesJar()
  withJavadocJar()
}

jreleaser {
  project {
    name = "PolicyEngine"
    description = "PolicyEngine - PoE is a catalog driven policy engine"
    inceptionYear = "2024"
    license = "Apache-2.0"
    maintainer("Ivan Sokol")
    links {
      homepage = "https://ivsokol.github.io/poe"
      license = "https://opensource.org/licenses/Apache-2.0"
    }
    java {
      groupId = "com.github.ivsokol"
      artifactId = "poe"
    }
    signing {
      setActive("ALWAYS")
      armored = true
    }
    release { github { overwrite = true } }
    deploy {
      maven {
        mavenCentral {
          create("maven-central") {
            setActive("NEVER")
            url = "https://central.sonatype.com/api/v1/publisher"
            stagingRepository("build/staging-deploy")
            applyMavenCentralRules = true
          }
        }
        github {
          create("github") {
            setActive("ALWAYS")
            stagingRepository("build/staging-deploy")
          }
        }
      }
    }
  }
}

publishing {
  publications {
    create<MavenPublication>("mavenKotlin") {
      groupId = "com.github.ivsokol"
      artifactId = "poe"
      version = project.version.toString()
      from(components["java"])

      pom {
        name = "PolicyEngine"
        description =
            "PolicyEngine - PoE is a catalog driven policy engine written in Kotlin. It provides a flexible and extensible framework for defining and evaluating policies and conditions based on the provided context."
        url = "https://ivsokol.github.io/poe"
        licenses {
          license {
            name = "The Apache License, Version 2.0"
            url = "https://opensource.org/licenses/Apache-2.0"
          }
        }
        developers {
          developer {
            id = "ivsokol"
            name = "Ivan Sokol"
            email = "ivan.sokol@gmail.com"
          }
        }
        scm {
          connection = "scm:git:https://ivsokol.github.io/poe.git"
          url = "https://ivsokol.github.io/poe"
        }
      }
    }
  }
  repositories { maven { url = uri(layout.buildDirectory.dir("staging-deploy")) } }
}

tasks.withType<KotlinCompile> { compilerOptions { jvmTarget.set(JvmTarget.JVM_21) } }

kotlin { jvmToolchain(21) }

project.tasks.getByName("assemble").finalizedBy("spotlessApply")

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
  kotlin { ktfmt() }
  kotlinGradle {
    target("*.gradle.kts")
    ktfmt()
  }
  json {
    target("src/**/*.json")
    simple().indentWithSpaces(2)
  }
}

kover.reports.verify.rule { minBound(90) }
