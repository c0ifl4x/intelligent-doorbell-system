import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

subprojects {
  val projectLibs = rootProject.libs
  pluginManager.withPlugin(projectLibs.plugins.kotlin.jvm.id()) {
    val javaExt = extensions.getByType<JavaPluginExtension>().apply {
      toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
      }
    }
    val jdk21Launcher = extensions.getByType<JavaToolchainService>().launcherFor(javaExt.toolchain)
    tasks.withType<JavaExec>().configureEach {
      javaLauncher.set(jdk21Launcher)
    }
    tasks.withType<KotlinCompile>().configureEach {
      compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
      }
    }
    tasks.withType<Test>().configureEach {
      useJUnitPlatform()
    }
  }
}

fun Provider<PluginDependency>.id(): String = get().pluginId

fun envOrDefault(name: String, default: String): String =
  System.getenv(name)?.takeIf { it.isNotBlank() } ?: default
