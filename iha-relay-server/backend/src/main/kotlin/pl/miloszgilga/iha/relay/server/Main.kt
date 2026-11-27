package pl.miloszgilga.iha.relay.server

import org.slf4j.bridge.SLF4JBridgeHandler
import pl.miloszgilga.iha.relay.server.bootstrap.LifecycleManager
import pl.miloszgilga.iha.relay.server.mqtt.MqttConnectionHandler
import pl.miloszgilga.iha.relay.server.mqtt.MqttDoorbellOnRingEventHandler
import pl.miloszgilga.iha.relay.server.mqtt.MqttEnvStatusHandler
import pl.miloszgilga.iha.relay.server.mqtt.MqttOnDoorbellClientRingHandler
import pl.miloszgilga.iha.relay.server.mqtt.MqttOnDoorbellModeSetHandler
import pl.miloszgilga.iha.relay.server.mqtt.MqttService
import pl.miloszgilga.iha.relay.server.net.MdnsService
import pl.miloszgilga.iha.relay.server.net.MdnsServiceDefinition
import pl.miloszgilga.iha.relay.server.net.NetworkProvider
import pl.miloszgilga.iha.relay.server.security.Permission
import pl.miloszgilga.iha.relay.server.security.permissionManagerOf
import pl.miloszgilga.iha.relay.server.ws.WsSessionRegistry

fun main() {
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

  LifecycleManager.runSafely {
    val permissionManager = permissionManagerOf<Permission>()

    val wsSessionRegistry = WsSessionRegistry(permissionManager)

    MqttService(
      port = 1883,
      brokerClientId = "HomeAutomationRelayMqttBroker",
      connectionCallback = MqttConnectionHandler(),
      messageHandlers = setOf(
        MqttDoorbellOnRingEventHandler(wsSessionRegistry),
        MqttEnvStatusHandler(wsSessionRegistry),
        MqttOnDoorbellClientRingHandler(wsSessionRegistry),
        MqttOnDoorbellModeSetHandler(wsSessionRegistry),
      )
    ).startManaged()

    MdnsService(
      address = NetworkProvider.lanInetAddress,
      MdnsServiceDefinition(
        type = "_mqtt._tcp.local.",
        scheme = "mqtt",
        port = 1883,
        name = "HomeAutomationRelayMqttBroker",
        description = "Intelligent home automation MQTT broker",
      ),
    ).startManaged()
  }
}
