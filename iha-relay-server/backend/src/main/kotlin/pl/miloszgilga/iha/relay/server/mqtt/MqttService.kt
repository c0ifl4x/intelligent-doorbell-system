package pl.miloszgilga.iha.relay.server.mqtt

import io.github.oshai.kotlinlogging.KotlinLogging
import io.moquette.broker.Server
import io.moquette.broker.config.IConfig
import io.moquette.broker.config.MemoryConfig
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.handler.codec.mqtt.MqttFixedHeader
import io.netty.handler.codec.mqtt.MqttMessageType
import io.netty.handler.codec.mqtt.MqttPublishMessage
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader
import io.netty.handler.codec.mqtt.MqttQoS
import pl.miloszgilga.iha.relay.server.bootstrap.ComponentLifecycle
import pl.miloszgilga.iha.relay.server.bootstrap.CriticalException
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.ObjectNode
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.io.IOException
import java.util.Properties
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private val log = KotlinLogging.logger {}

internal class MqttService(
  private val objectMapper: ObjectMapper = jacksonObjectMapper(),
  private val port: Int,
  private val brokerClientId: String,
  connectionCallback: MqttConnectionCallback,
  messageHandlers: Set<MqttMessageHandler> = emptySet(),
) : ComponentLifecycle {

  private val mqttBroker = Server()
  private val executorService: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()
  private val brokerConfig: IConfig = createConfig(port)
  private val authenticator = MqttDeviceAuthenticator()
  private val eventInterceptor = MqttPublishHandler(
    handlers = messageHandlers,
    executorService = executorService,
    connectionCallback = connectionCallback,
    objectMapper = objectMapper,
  )

  init {
    log.info { "MqttService initialized with ${messageHandlers.size} message handlers" }
  }

  override fun init() {
    log.info { "Starting MQTT broker on port $port..." }
    try {
      mqttBroker.startServer(brokerConfig, listOf(eventInterceptor), null, authenticator, null)
      log.info { "MQTT broker is up and running" }
    } catch (ex: IOException) {
      throw CriticalException(ex.message, ex)
    }
  }

  override fun close() {
    mqttBroker.stopServer()
    executorService.shutdown()
    log.info { "Virtual thread executor and MQTT broker were successfully closed" }
  }

  fun publish(topic: MqttOutboundTopic, qos: MqttQoS, retained: Boolean, payload: ObjectNode? = null) {
    val payloadBuf = when (payload) {
      null -> Unpooled.EMPTY_BUFFER
      else -> Unpooled.copiedBuffer(objectMapper.writeValueAsString(payload), Charsets.UTF_8)
    }
    internalPublish(topic.path, payloadBuf, qos, retained)
  }

  private fun internalPublish(path: String, payload: ByteBuf, qos: MqttQoS, retained: Boolean) {
    log.debug { "Internal publish to topic: $path [qos: $qos, retain: $retained, bytes: ${payload.readableBytes()}]" }
    val fixedHeader = MqttFixedHeader(MqttMessageType.PUBLISH, false, qos, retained, 0)
    val varHeader = MqttPublishVariableHeader(path, 0)
    val message = MqttPublishMessage(fixedHeader, varHeader, payload)
    mqttBroker.internalPublish(message, brokerClientId)
  }

  private fun createConfig(port: Int): IConfig = MemoryConfig(
    Properties().apply {
      setProperty("port", port.toString())
      setProperty("host", "0.0.0.0")
      setProperty("allow_anonymous", "false")
    },
  )
}
