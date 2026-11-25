package pl.miloszgilga.iha.relay.server.mqtt

internal interface MqttTopic {
  val path: String
}

// device -> relay
internal interface MqttInboundTopic : MqttTopic

internal enum class MqttAppInboundTopic(override val path: String) : MqttInboundTopic {
  DOORBELL_ON_RING("ids/doorbell/on/ring"),
  ENV_STATUS("ids/env/status"),
  ON_DOORBELL_MODE_SET("ids/on/doorbell/mode/set"),
  ON_DOORBELL_RING("ids/on/doorbell/ring"),
  ;
}

// relay -> device
internal interface MqttOutboundTopic : MqttTopic

internal enum class MqttAppOutboundTopic(override val path: String) : MqttOutboundTopic {
  DOORBELL_MODE_SET("ids/doorbell/mode/set"),
  DOORBELL_RING("ids/doorbell/ring"),
  ;
}
