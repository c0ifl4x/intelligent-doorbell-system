plugins {
  alias(libs.plugins.esp.idf)
}

espIdf {
  chip.set("esp32c3")
}
