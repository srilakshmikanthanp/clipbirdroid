package com.srilakshmikanthanp.clipbirdroid.types.enums

enum class ErrorCode(val value: Byte = 0x00) {
  /**
   * Allowed Error Codes
   */
  CodingError(0x00), InvalidPacket(0x01);

  /**
   * Companion Object
   */
  companion object {
    /**
     * Convert Byte to ErrorCode
     */
    fun fromByte(value: Byte): ErrorCode = when (value) {
      InvalidPacket.value -> InvalidPacket
      InvalidPacket.value -> InvalidPacket
      else -> throw IllegalArgumentException("Invalid ErrorCode value: $value")
    }

    /**
     * Convert ErrorCode to Byte
     */
    fun toByte(status: ErrorCode): Byte {
      return status.value
    }
  }
}
