package pl.miloszgilga.iha.relay.server.ws

import io.github.oshai.kotlinlogging.KotlinLogging
import pl.miloszgilga.iha.relay.server.security.Permission
import pl.miloszgilga.iha.relay.server.security.PermissionManager

private val log = KotlinLogging.logger {}

internal class WsSessionRegistry(private val permissionManager: PermissionManager<Permission>) {

  fun broadcast(op: OpCode) {
  }

  fun broadcast(op: OpCode, payload: Any, vararg requiredPermissions: Permission) {
  }
}
