package pl.miloszgilga.iha.relay.server.ws

interface OpCode {
  val code: Int

  val category: Int
    get() = (code shr 16) and 0xFFFF

  val actionCode: Int
    get() = code and 0xFFFF

  companion object {
    private val lookup = mutableMapOf<Int, OpCode>()

    fun registerAll(codes: Iterable<OpCode>) = codes.forEach { lookup[it.code] = it }

    // category: (0xXXXX0000), action: (0x0000XXXX)
    fun combine(category: Int, action: Int) = (category shl 16) or (action and 0xFFFF)

    fun fromInt(code: Int, fallback: OpCode) = lookup[code] ?: fallback
  }
}

enum class AppOpCode(category: Int, action: Int) : OpCode {
  HEARTBEAT(1, 1),              // 65537
  ENV_STATUS(2, 1),             // 131073
  DOORBELL_RING(2, 2),          // 131074
  DOORBELL_MODE_SET(2, 3),      // 131075
  DOORBELL_MANUALLY_RING(2, 4), // 131076
  ;

  override val code: Int = OpCode.combine(category, action)

  companion object {
    init {
      OpCode.registerAll(entries)
    }
  }
}
