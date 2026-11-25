package pl.miloszgilga.iha.relay.server.bootstrap

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.Closeable
import java.util.Collections
import kotlin.concurrent.thread
import kotlin.system.exitProcess

internal interface ContentInitializer {
  fun init()
}

internal interface ComponentLifecycle : ContentInitializer, Closeable

private val log = KotlinLogging.logger {}

internal object LifecycleManager : AutoCloseable {
  private val resources: MutableList<AutoCloseable> = Collections.synchronizedList(mutableListOf())

  init {
    Runtime.getRuntime().addShutdownHook(
      thread(start = false, name = "relay-shutdown-hook") {
        log.info { "Shutting down relay server resources..." }
        close()
      },
    )
  }

  fun <T : AutoCloseable> T.closeOnShutdown(): T = apply {
    resources += this
  }

  fun <T : ComponentLifecycle> T.startManaged(): T = closeOnShutdown().apply { init() }

  fun runSafely(block: LifecycleManager.() -> Unit) {
    try {
      block()
    } catch (ex: CriticalException) {
      log.error(ex) { "CRITICAL EXCEPTION: ${ex.message}" }
      exitProcess(-1)
    }
  }

  override fun close() {
    synchronized(resources) {
      resources.asReversed().forEach { it.closeQuietly() }
      resources.clear()
    }
  }

  private fun AutoCloseable.closeQuietly() {
    runCatching { close() }
      .onFailure { ex -> log.error(ex) { "Unable to close resource, cause: ${ex.message}" } }
  }
}
