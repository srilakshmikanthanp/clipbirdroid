package com.srilakshmikanthanp.clipbirdroid.types.enums

/**
 * Enum Class Used for Authentication packet
 */
enum class AuthStatus(private val value: Byte = 0x00) {
  AuthOkay(0x00), AuthFail(0x01);

  companion object {
    fun fromByte(value: Byte): AuthStatus = when (value) {
        AuthOkay.value -> AuthOkay
        AuthFail.value -> AuthFail
        else -> throw IllegalArgumentException("Invalid AuthStatus value: $value")
    }

    fun toByte(status: AuthStatus): Byte {
      return status.value
    }
  }
}
