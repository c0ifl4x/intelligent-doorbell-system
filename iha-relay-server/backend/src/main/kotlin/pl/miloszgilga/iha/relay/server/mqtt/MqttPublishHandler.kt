package pl.miloszgilga.iha.relay.server.mqtt

import io.github.oshai.kotlinlogging.KotlinLogging
import io.moquette.interception.AbstractInterceptHandler
import io.moquette.interception.messages.InterceptConnectMessage
import io.moquette.interception.messages.InterceptConnectionLostMessage
import io.moquette.interception.messages.InterceptDisconnectMessage
import io.moquette.interception.messages.InterceptPublishMessage
import io.netty.util.ReferenceCountUtil
import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.ObjectNode
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.util.concurrent.ExecutorService

private val log = KotlinLogging.logger {}

internal class MqttPublishHandler(
  handlers: Set<MqttMessageHandler>,
  private val executorService: ExecutorService,
  private val connectionCallback: MqttConnectionCallback,
  private val objectMapper: ObjectMapper = jacksonObjectMapper(),
) : AbstractInterceptHandler() {

  private val routes: Map<String, MqttMessageHandler> = handlers
    .associateBy { it.topic.path }
    .onEach { (path, _) -> log.info { "Registered handler for topic: $path" } }

  override fun getID(): String = "vt-mqtt-publisher-handler"

  override fun onPublish(msg: InterceptPublishMessage) {
    executorService.submit { processMessage(msg.topicName, msg.extractPayloadAndRelease()) }
  }

  override fun onConnect(msg: InterceptConnectMessage) = notifyConnection { onConnected(msg.clientID, msg.username) }

  override fun onConnectionLost(msg: InterceptConnectionLostMessage) =
    notifyConnection { onDisconnected(msg.clientID, false) }

  override fun onDisconnect(msg: InterceptDisconnectMessage) = notifyConnection { onDisconnected(msg.clientID, true) }

  override fun onSessionLoopError(throwable: Throwable) {
    log.error(throwable) { "Session loop error in $id" }
  }

  private inline fun notifyConnection(crossinline action: MqttConnectionCallback.() -> Unit) {
    executorService.submit { connectionCallback.action() }
  }

  private fun InterceptPublishMessage.extractPayloadAndRelease(): String = when (val buffer = payload) {
    null -> ""
    else -> try {
      buffer.toString(Charsets.UTF_8)
    } finally {
      ReferenceCountUtil.release(buffer) // release Netty's native memory
    }
  }

  private fun processMessage(topic: String, payload: String) = when (val handler = routes[topic]) {
    null -> log.trace { "Message rejected - no registered handler for topic: $topic" }
    else -> when {
      payload.isBlank() -> {
        log.debug { "Received empty payload for topic: $topic, executing handler with empty JSON." }
        safeInvokeHandler(handler, objectMapper.createObjectNode())
      }

      else -> parseAndHandleJson(handler, topic, payload)
    }
  }

  private fun parseAndHandleJson(handler: MqttMessageHandler, topic: String, payload: String) = try {
    when (val tree = objectMapper.readTree(payload)) {
      is ObjectNode -> safeInvokeHandler(handler, tree)
      else -> log.warn { "Payload for topic $topic is not a JSON Object, received: $payload" }
    }
  } catch (ex: JacksonException) {
    log.warn(ex) { "Invalid JSON syntax for topic $topic, payload: '$payload'" }
  }

  private fun safeInvokeHandler(handler: MqttMessageHandler, json: ObjectNode) = try {
    handler.handle(json)
  } catch (ex: Exception) {
    log.error(ex) { "Error occurred while executing handler for topic: ${handler.topic.path}, error: ${ex.message}" }
  }
}
