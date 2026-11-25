package pl.miloszgilga.iha.relay.server.net

import io.github.oshai.kotlinlogging.KotlinLogging
import pl.miloszgilga.iha.relay.server.bootstrap.ComponentLifecycle
import pl.miloszgilga.iha.relay.server.bootstrap.CriticalException
import java.io.IOException
import java.net.Inet4Address
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

private val log = KotlinLogging.logger {}

internal data class MdnsServiceDefinition(
  val type: String,
  val scheme: String,
  val port: Int,
  val name: String,
  val description: String,
)

internal class MdnsService(
  private val address: InetAddress,
  private vararg val services: MdnsServiceDefinition,
) : ComponentLifecycle {

  private var jmdns: JmDNS? = null

  override fun init() = when {
    services.isEmpty() -> log.warn { "No mDNS services configured to register" }
    else -> registerServices()
  }

  override fun close() {
    jmdns?.run {
      unregisterAllServices()
      close()
      log.info { "mDNS service was successfully closed" }
    }
  }

  private fun registerServices() {
    var instance: JmDNS? = null
    try {
      log.info { "Initializing mDNS on IP address: ${address.hostAddress}" }
      instance = JmDNS.create(address)
      services.forEach { definition ->
        val serviceInfo = ServiceInfo.create(definition.type, definition.name, definition.port, definition.description)
        instance.registerService(serviceInfo)
        val serviceUri = createServiceUri(definition, serviceInfo.inet4Addresses)
        log.info { "Successfully registered mDNS service: $serviceUri" }
      }
      jmdns = instance
    } catch (ex: IOException) {
      instance?.runCatching { close() }
      throw CriticalException(ex.message, ex)
    }
  }

  private fun createServiceUri(definition: MdnsServiceDefinition, ipv4Addresses: Array<Inet4Address>): String =
    when (val primaryAddress = ipv4Addresses.firstOrNull()) {
      null -> throw IOException("No IPv4 addresses were resolved for service '${definition.name}'")
      else -> "${definition.scheme}://${primaryAddress.hostAddress}:${definition.port}"
    }
}
