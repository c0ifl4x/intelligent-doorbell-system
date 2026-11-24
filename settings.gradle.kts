rootProject.name = "intelligent-home-automation"

pluginManagement {
  repositories {
    mavenLocal()
    maven {
      name = "ReposiliteReleases"
      url = uri("https://m2.miloszgilga.pl/releases")
    }
    gradlePluginPortal()
  }
}

include("iha-doorbell-firmware")
include("iha-relay-server:backend")
include("iha-relay-server:ui")
