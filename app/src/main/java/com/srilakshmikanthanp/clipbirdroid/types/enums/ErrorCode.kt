package com.srilakshmikanthanp.clipbirdroid.types.enums

enum class ErrorCode(val value: Int = 0x00) {
  /**
   * Allowed Error Codes
   */
  CodingError(0x00), InvalidPacket(0x01);

  /**
   * Companion Object
   */
  companion object {
    fun fromByte(value: Int): ErrorCode = when (value) {
      InvalidPacket.value -> InvalidPacket
      InvalidPacket.value -> InvalidPacket
      else -> throw IllegalArgumentException("Invalid ErrorCode value: $value")
    }

    fun toByte(status: ErrorCode): Int {
      return status.value
    }
  }
}
