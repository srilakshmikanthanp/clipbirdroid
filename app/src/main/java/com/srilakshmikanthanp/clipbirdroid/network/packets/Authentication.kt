package com.srilakshmikanthanp.clipbirdroid.network.packets

import com.srilakshmikanthanp.clipbirdroid.types.enums.AuthStatus
import java.nio.ByteBuffer

/**
 * Data Class Used for Authentication packet
 */
class Authentication(
  @JvmField var packetLength: Int,
  @JvmField var packetType: Byte,
  @JvmField var authStatus: Byte
) {
  /**
   * Allowed packet Types
   */
  enum class PacketType(val value: Byte = 0x01) {
    AuthStatus(0x01),
  }

  /**
   * Set the Packet Length
   */
  fun setPacketLength(length: Int) {
    this.packetLength = length
  }

  /**
   * Get the Packet Length
   */
  fun getPacketLength(): Int {
    return this.packetLength
  }

  /**
   * Set the Packet Type
   */
  fun setPacketType(type: Byte) {
    // check packetType
    if (type != PacketType.AuthStatus.value) {
      throw IllegalArgumentException("Invalid PacketType value: $type")
    }

    this.packetType = type
  }

  /**
   * Get the Packet Type
   */
  fun getPacketType(): Byte {
    return this.packetType
  }

  /**
   * Set the Auth Status
   */
  fun setAuthStatus(status: Byte) {
    this.authStatus = AuthStatus.fromByte(status).value
  }

  /**
   * Get the Auth Status
   */
  fun getAuthStatus(): Byte {
    return this.authStatus
  }

  /**
   * Size of Packet
   */
  fun size(): Int {
    return (Int.SIZE_BYTES + Byte.SIZE_BYTES)
  }

  /**
   * Companion Object
   */
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

      // check packetType
      if (packetType != PacketType.AuthStatus.value) {
        throw IllegalArgumentException("Invalid PacketType value: $packetType")
      }

      // return Authentication
      return Authentication(
        packetLength,
        packetType,
        authStatus.value
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
      byteBuffer.put(auth.authStatus)

      // return Bytearray
      return byteBuffer.array()
    }
  }
}
