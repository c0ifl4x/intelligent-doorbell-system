package pl.miloszgilga.iha.relay.server.mqtt

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.Level

private val log = KotlinLogging.logger {}

internal interface MqttConnectionCallback {
  fun onConnected(clientId: String, username: String)

  fun onDisconnected(clientId: String, wasLost: Boolean)
}

internal class MqttConnectionHandler : MqttConnectionCallback {
  override fun onConnected(clientId: String, username: String) {
    log.info { "Client CONNECTED, ID: '$clientId', username: '$username'" }
  }

  override fun onDisconnected(clientId: String, wasLost: Boolean): Unit = when (wasLost) {
    true -> Level.WARN to "Client CONNECTION LOST (timeout/error), ID: '$clientId'"
    false -> Level.INFO to "Client DISCONNECTED (gracefully), ID: '$clientId'"
  }.let { (logLevel, msg) -> log.at(logLevel) { message = msg } }
}
