import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  application
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.jib)
  alias(libs.plugins.web.embed)
}

repositories {
  maven {
    url = uri("https://www.jitpack.io") // for moquette broker
  }
}

kotlin {
  jvmToolchain(21)
}

dependencies {
  implementation(libs.javalin)
  implementation(libs.jackson.kotlin)

  implementation(libs.flyway.core)
  implementation(libs.jdbi.core)
  implementation(libs.jdbi.kotlin)
  implementation(libs.jdbi.sqlobject)
  implementation(libs.sqlite.jdbc)

  implementation(kotlin("reflect"))

  implementation(libs.jmdns)
  implementation(libs.jul.to.slf4j)
  implementation(libs.kotlin.logging)
  implementation(libs.logback.classic)
  implementation(libs.moquette.broker) {
    exclude(group = "org.slf4j", module = "slf4j-reload4j")
  }

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.platform)
  testImplementation(libs.mockk)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.mqtt.paho)
}

application {
  mainClass.set("pl.miloszgilga.iha.relay.server.MainKt")
}

tasks.named<JavaExec>("run") {
  systemProperty("file.encoding", "UTF-8")
  systemProperty("logback.configurationFile", "logback-dev.xml")
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
  testLogging {
    events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    showStandardStreams = true
  }
  // -Xshare:off: disables class data sharing
  jvmArgs("-Xshare:off")
}

jib {
  from {
    image = "gcr.io/distroless/java21-debian12:nonroot"
    platforms {
      platform {
        architecture = "amd64"
        os = "linux"
      }
      platform {
        architecture = "arm64"
        os = "linux"
      }
    }
  }
  to {
    image = "ghcr.io/c0ifl4x/iha-relay-server"
    tags = setOf("latest")
    auth {
      username = System.getenv("GITHUB_ACTOR") ?: ""
      password = System.getenv("GITHUB_TOKEN") ?: ""
    }
  }
  container {
    ports = listOf("8080")
    jvmFlags = listOf(
      "-XX:+UseContainerSupport",
      "-XX:MaxRAMPercentage=75.0",
      "-Dfile.encoding=UTF-8"
    )
    labels = mapOf(
      "org.opencontainers.image.source" to "https://github.com/c0ifl4x/intelligent-home-automation",
    )
    creationTime.set("USE_CURRENT_TIMESTAMP")
  }
}
