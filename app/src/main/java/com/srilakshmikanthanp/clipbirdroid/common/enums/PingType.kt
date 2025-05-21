package com.srilakshmikanthanp.clipbirdroid.common.enums

enum class PingType (val value: Int = 0x00) {
  /**
   * Allowed Ping Types
   */
  Ping(0x00), Pong(0x01);

  /**
   * Convert to Byte
   */
  fun toByte(status: PingType): Int {
    return status.value
  }

  /**
   * Companion Object
   */
  companion object {
    fun fromInt(value: Int): PingType = when (value) {
      Ping.value -> Ping
      Pong.value -> Pong
      else -> throw IllegalArgumentException("Invalid PingType value: $value")
    }
  }
}
