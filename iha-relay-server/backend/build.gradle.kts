plugins {
  application
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.jib)
  alias(libs.plugins.web.embed)
}

dependencies {
  implementation(libs.logback.classic)
}

application {
  mainClass.set("pl.miloszgilga.iha.relay.server.MainKt")
}

jib {
  from {
    image = "gcr.io/distroless/java21-debian12:nonroot"
  }
  to {
    image = "ghcr.io/c0ifl4x/iha-relay-server"
    tags = setOf("latest")
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
