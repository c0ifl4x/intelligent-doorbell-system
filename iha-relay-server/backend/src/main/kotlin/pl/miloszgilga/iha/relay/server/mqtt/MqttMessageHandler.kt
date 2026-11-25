package pl.miloszgilga.iha.relay.server.mqtt

import pl.miloszgilga.iha.relay.server.security.Permission
import pl.miloszgilga.iha.relay.server.ws.AppOpCode
import pl.miloszgilga.iha.relay.server.ws.WsSessionRegistry
import tools.jackson.databind.node.ObjectNode

internal interface MqttMessageHandler {
  val topic: MqttTopic

  fun handle(payload: ObjectNode)
}

internal class MqttDoorbellOnRingEventHandler(private val wsSessionRegistry: WsSessionRegistry) : MqttMessageHandler {
  override val topic: MqttInboundTopic = MqttAppInboundTopic.DOORBELL_ON_RING

  override fun handle(payload: ObjectNode) {
    // TODO: persist in db
    wsSessionRegistry.broadcast(AppOpCode.DOORBELL_RING);
  }
}

internal class MqttEnvStatusHandler(private val wsSessionRegistry: WsSessionRegistry) : MqttMessageHandler {
  override val topic: MqttInboundTopic = MqttAppInboundTopic.ENV_STATUS

  override fun handle(payload: ObjectNode) {
    // TODO: persist in db
    wsSessionRegistry.broadcast(AppOpCode.ENV_STATUS, payload, Permission.ENV_STATUS_VIEWER)
  }
}

internal class MqttOnDoorbellClientRingHandler(private val wsSessionRegistry: WsSessionRegistry) : MqttMessageHandler {
  override val topic: MqttInboundTopic = MqttAppInboundTopic.ON_DOORBELL_RING

  override fun handle(payload: ObjectNode) {
    // TODO: persist in db
    wsSessionRegistry.broadcast(AppOpCode.DOORBELL_MANUALLY_RING);
  }
}

internal class MqttOnDoorbellModeSetHandler(private val wsSessionRegistry: WsSessionRegistry) : MqttMessageHandler {
  override val topic: MqttInboundTopic = MqttAppInboundTopic.ON_DOORBELL_MODE_SET

  override fun handle(payload: ObjectNode) {
    val isSilent = payload.path("is_silent").booleanValue(false)
    // TODO: persist in db
    wsSessionRegistry.broadcast(
      AppOpCode.DOORBELL_MODE_SET,
      mapOf(
        "isSilent" to isSilent,
        "updatedAt" to System.currentTimeMillis(),
      ),
    )
  }
}
