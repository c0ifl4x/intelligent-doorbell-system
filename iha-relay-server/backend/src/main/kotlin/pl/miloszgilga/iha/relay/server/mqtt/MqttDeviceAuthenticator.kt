package pl.miloszgilga.iha.relay.server.mqtt

import io.github.oshai.kotlinlogging.KotlinLogging
import io.moquette.broker.security.IAuthenticator

private val log = KotlinLogging.logger {}

internal class MqttDeviceAuthenticator : IAuthenticator {
  override fun checkValid(clientId: String?, username: String?, password: ByteArray?): Boolean {
    val user = username ?: run {
      log.warn { "Rejected connection for client '$clientId': missing username" }
      return false
    }
    val pass = password ?: run {
      log.warn { "Rejected connection for client '$clientId': missing password" }
      return false
    }
    // TODO: validate via custom TCP handshake protocol
    return true;
  }
}
