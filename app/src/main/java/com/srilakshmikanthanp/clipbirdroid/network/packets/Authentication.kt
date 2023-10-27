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
class Authentication(private var authStatus: AuthStatus) {
  /**
   * Allowed packet Types
   */
  enum class PacketType(val value: Int) {
    AuthStatus(0x01),
  }

  // Private Fields
  private var packetType: PacketType
  private var packetLength: Int

  // init
  init {
    this.packetType = PacketType.AuthStatus
    this.packetLength = this.size()
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
  fun setPacketType(type: PacketType) {
    this.packetType = type
  }

  /**
   * Get the Packet Type
   */
  fun getPacketType(): PacketType {
    return this.packetType
  }

  /**
   * Set the Auth Status
   */
  fun setAuthStatus(status: AuthStatus) {
    this.authStatus = status
  }

  /**
   * Get the Auth Status
   */
  fun getAuthStatus(): AuthStatus {
    return this.authStatus
  }

  /**
   * Size of Packet
   */
  fun size(): Int {
    return (Int.SIZE_BYTES + Int.SIZE_BYTES + Int.SIZE_BYTES)
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
    buffer.putInt(this.packetType.value)
    buffer.putInt(this.authStatus.value)

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
      val packetType: Int
      val authStatus: Int

      // try to get bytes
      try {
        packetLength = buffer.int
        packetType = buffer.int
      } catch (e: BufferUnderflowException) {
        throw MalformedPacket(ErrorCode.CodingError, "Invalid Packet Length")
      }

      // check the packet type
      if (packetType != PacketType.AuthStatus.value) {
        throw NotThisPacket("Not Authentication Packet")
      }

      // try to get bytes
      try {
        authStatus = buffer.int
      } catch (e: BufferUnderflowException) {
        throw MalformedPacket(ErrorCode.CodingError, "Invalid Packet Length")
      }

      // check authStatus
      if (!allowedAuthStatus.contains(authStatus)) {
        throw MalformedPacket(ErrorCode.CodingError, "Invalid AuthStatus value")
      }

      // done return
      return Authentication(AuthStatus.fromInt(authStatus))
    }
  }
}
