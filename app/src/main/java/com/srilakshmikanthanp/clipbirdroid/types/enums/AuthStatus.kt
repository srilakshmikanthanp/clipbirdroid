package com.srilakshmikanthanp.clipbirdroid.types.enums

/**
 * Enum Class Used for Authentication packet
 */
enum class AuthStatus(val value: Byte = 0x00) {
  /**
   * Allowed Auth Status
   */
  AuthOkay(0x00), AuthFail(0x01);

  /**
   * Companion Object
   */
  companion object {
    /**
     * Convert Byte to AuthStatus
     */
    fun fromByte(value: Byte): AuthStatus = when (value) {
        AuthOkay.value -> AuthOkay
        AuthFail.value -> AuthFail
        else -> throw IllegalArgumentException("Invalid AuthStatus value: $value")
    }

    /**
     * Convert AuthStatus to Byte
     */
    fun toByte(status: AuthStatus): Byte {
      return status.value
    }
  }
}
