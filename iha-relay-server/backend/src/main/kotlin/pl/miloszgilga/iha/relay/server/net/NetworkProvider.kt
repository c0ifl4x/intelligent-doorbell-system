package pl.miloszgilga.iha.relay.server.net

import io.github.oshai.kotlinlogging.KotlinLogging
import pl.miloszgilga.iha.relay.server.bootstrap.CriticalException
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException

private val log = KotlinLogging.logger {}

private val forbiddenNames = setOf("docker", "veth", "wsl")
private val forbiddenDisplayNames = setOf("virtual", "vmware", "hyper-v")

internal object NetworkProvider {
  private var cachedAddress: InetAddress? = null

  val lanInetAddress: InetAddress
    get() = when (val cached = cachedAddress) {
      null -> resolveAndCacheAddress()
      else -> cached.also { log.debug { "Returning cached IP address: ${it.hostAddress}" } }
    }

  private fun resolveAndCacheAddress(): InetAddress = try {
    log.debug { "Starting network interfaces scan for LAN address..." }
    val resolved = scanForLanAddress()
      ?: getFallbackLocalHost()
      ?: throw CriticalException("Unable to resolve valid LAN or LocalHost IP address")
    resolved.also { cachedAddress = it }
  } catch (ex: SocketException) {
    throw CriticalException(ex.message, ex)
  }

  private fun scanForLanAddress(): InetAddress? = NetworkInterface.getNetworkInterfaces()
    ?.asSequence()
    ?.filter { it.isEligible() }
    ?.firstNotNullOfOrNull { it.findValidIpv4Address() }

  private fun NetworkInterface.isEligible(): Boolean {
    val lowerName = name.lowercase()
    val lowerDisplayName = displayName.lowercase()
    return when {
      !isUp -> false.also {
        log.trace { "Skipping interface '$lowerName' - it is down" }
      }

      isLoopback -> false.also {
        log.trace { "Skipping interface '$lowerName' - it is a loopback" }
      }

      isVirtual -> false.also {
        log.trace { "Skipping interface '$lowerName' - it is marked as virtual" }
      }

      forbiddenNames.any { it in lowerName } || forbiddenDisplayNames.any { it in lowerDisplayName } -> false.also {
        log.debug { "Skipping interface '$lowerName' ('$lowerDisplayName') - it is blacklisted" }
      }

      else -> true.also {
        log.trace { "Checking addresses for potentially valid interface: '$lowerName' ('$lowerDisplayName')" }
      }
    }
  }

  private fun NetworkInterface.findValidIpv4Address(): InetAddress? = inetAddresses.asSequence().firstOrNull { addr ->
    when {
      addr is Inet4Address && !addr.isLinkLocalAddress -> true.also {
        log.info { "Found valid LAN IPv4 address: ${addr.hostAddress} (on interface '$name')" }
      }

      else -> false.also {
        log.trace { "Rejecting address ${addr.hostAddress} - not ipv4 or is link-local" }
      }
    }
  }

  private fun getFallbackLocalHost(): InetAddress? {
    log.warn { "No valid LAN address found on network interfaces, attempting to get LocalHost..." }
    return runCatching { InetAddress.getLocalHost() }
      .onSuccess { localHost -> log.info { "Fallback to LocalHost address: ${localHost.hostAddress}" } }
      .onFailure { ex -> log.error { "Failed to get fallback localhost address: ${ex.message}" } }
      .getOrNull()
  }
}
