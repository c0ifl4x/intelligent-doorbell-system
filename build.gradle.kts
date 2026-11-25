plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.jib) apply false
}

allprojects {
  group = "pl.miloszgilga.iha"
  version = envOrDefault("RELEASE_VERSION", "0.0.1-local-dev")
  repositories {
    mavenCentral()
  }
}

fun envOrDefault(name: String, default: String): String =
  System.getenv(name)?.takeIf { it.isNotBlank() } ?: default
