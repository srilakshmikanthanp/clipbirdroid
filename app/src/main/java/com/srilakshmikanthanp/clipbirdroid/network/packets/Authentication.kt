package com.srilakshmikanthanp.clipbirdroid.network.packets

import com.srilakshmikanthanp.clipbirdroid.types.enums.AuthStatus
import java.nio.ByteBuffer

/**
 * Data Class Used for Authentication packet
 */
data class Authentication(
  var packetLength: Int,
  val packetType: Byte = 0x01,
  var authStatus: AuthStatus,
) {
  companion object {
    /**
     * Create From byte array In BigEndian
     */
    fun fromByteArray(byteArray: ByteArray): Authentication {
      // create ByteBuffer from byte array
      val buffer = ByteBuffer.wrap(byteArray)

      // read fields
      val packetLength = buffer.int
      val packetType = buffer.get()
      val authStatus = AuthStatus.fromByte(buffer.get())

      // return Authentication
      return Authentication(
        packetLength,
        packetType,
        authStatus,
      )
    }

    /**
     * Convert To Byte array Big Endian
     */
    fun toByteArray(auth: Authentication): ByteArray {
      // create ByteArray from the Class
      val byteBuffer = ByteBuffer.allocate(auth.packetLength)

      // Set the fields
      byteBuffer.putInt(auth.packetLength)
      byteBuffer.put(auth.packetType)
      byteBuffer.put(AuthStatus.toByte((auth.authStatus)))

      // return Bytearray
      return byteBuffer.array()
    }
  }
}
