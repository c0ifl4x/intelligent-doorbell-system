package pl.miloszgilga.iha.relay.server.security

internal interface BitmaskPermission {
  val shift: Int

  val bit: Long

  val admin: Boolean
    get() = false
}

internal enum class Permission(override val shift: Int, override val admin: Boolean = false) : BitmaskPermission {
  ADMIN(0, admin = true), // 1
  ENV_STATUS_VIEWER(1),     // 2
  ;

  override val bit: Long = 1L shl shift
}
