plugins {
  alias(libs.plugins.esp.idf)
}

espIdf {
  chip.set("esp32c3")
  usbipdAutoAttach.set(true)
}

dependencies {
  espComponents(project(":iha-firmware:components"))
}
