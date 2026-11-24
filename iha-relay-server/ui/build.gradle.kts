plugins {
  alias(libs.plugins.frontend.builder)
}

frontendBuilder {
  buildDirName.set("build")
  additionalDeletes.from(".svelte-kit")
}
