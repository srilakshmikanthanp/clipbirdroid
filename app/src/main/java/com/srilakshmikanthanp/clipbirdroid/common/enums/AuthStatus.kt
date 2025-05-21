package com.srilakshmikanthanp.clipbirdroid.common.enums

/**
 * Enum Class Used for Authentication packet
 */
enum class AuthStatus(val value: Int = 0x00) {
  /**
   * Allowed Auth Status
   */
  AuthOkay(0x00), AuthFail(0x01);

  /**
   * Convert to Byte
   */
  fun toInt(status: AuthStatus): Int {
    return status.value
  }

  /**
   * Companion Object
   */
  companion object {
    fun fromInt(value: Int): AuthStatus = when (value) {
        AuthOkay.value -> AuthOkay
        AuthFail.value -> AuthFail
        else -> throw IllegalArgumentException("Invalid AuthStatus value: $value")
    }
  }
}
