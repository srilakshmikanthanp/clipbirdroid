package com.srilakshmikanthanp.clipbirdroid.network.packets

import com.srilakshmikanthanp.clipbirdroid.types.enums.AuthStatus
import com.srilakshmikanthanp.clipbirdroid.types.enums.ErrorCode
import com.srilakshmikanthanp.clipbirdroid.types.except.MalformedPacket
import com.srilakshmikanthanp.clipbirdroid.types.except.NotThisPacket
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Data Class Used for Authentication packet
 */
class Authentication(
  @JvmField var packetLength: Int,
  @JvmField var packetType: Int,
  @JvmField var authStatus: Int
) {
  /**
   * Allowed packet Types
   */
  enum class PacketType(val value: Int = 0x01) {
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
  fun setPacketType(type: Int) {
    // check packetType
    if (type != PacketType.AuthStatus.value) {
      throw IllegalArgumentException("Invalid PacketType value: $type")
    }

    this.packetType = type
  }

  /**
   * Get the Packet Type
   */
  fun getPacketType(): Int {
    return this.packetType
  }

  /**
   * Set the Auth Status
   */
  fun setAuthStatus(status: Int) {
    this.authStatus = AuthStatus.fromInt(status).value
  }

  /**
   * Get the Auth Status
   */
  fun getAuthStatus(): Int {
    return this.authStatus
  }

  /**
   * Size of Packet
   */
  fun size(): Int {
    return (Int.SIZE_BYTES + Byte.SIZE_BYTES)
  }

  /**
   * Convert To Byte array Big Endian
   */
  fun toByteArray(): ByteArray {
    // Create ByteBuffer to serialize the Packet
    val buffer = ByteBuffer.allocate(this.size())

    // set order
    buffer.order(ByteOrder.BIG_ENDIAN)

    // put data
    buffer.putInt(this.packetLength)
    buffer.putInt(this.packetType)
    buffer.putInt(this.authStatus)

    // return ByteArray
    return buffer.array()
  }

  /**
   * Companion Object
   */
  companion object {
    /**
     * Create From byte array In BigEndian
     */
    fun fromByteArray(byteArray: ByteArray): Authentication {
      // allowed authStatus
      val allowedAuthStatus = AuthStatus.values().map { it.value }

      // Create ByteBuffer to deserialize the Packet
      val buffer = ByteBuffer.wrap(byteArray)

      // set order
      buffer.order(ByteOrder.BIG_ENDIAN)

      // get data
      val packetLength: Int
      val packetType:Int
      val authStatus: Int

      // trey to get bytes
      try {
        packetLength = buffer.int
        packetType = buffer.int
        authStatus = buffer.int
      } catch (e: BufferUnderflowException) {
        throw MalformedPacket(ErrorCode.CodingError, "Invalid Packet Length")
      }

      // check the packet type
      if (packetType != PacketType.AuthStatus.value) {
        throw NotThisPacket("Not Authentication Packet")
      }

      // check authStatus
      if (!allowedAuthStatus.contains(authStatus)) {
        throw MalformedPacket(ErrorCode.CodingError, "Invalid AuthStatus value: $authStatus")
      }

      // return Authentication
      return Authentication(packetLength, packetType, authStatus)
    }
  }
}
