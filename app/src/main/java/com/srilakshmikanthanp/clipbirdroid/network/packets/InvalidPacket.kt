package com.srilakshmikanthanp.clipbirdroid.network.packets

import com.srilakshmikanthanp.clipbirdroid.types.enums.ErrorCode
import com.srilakshmikanthanp.clipbirdroid.types.except.MalformedPacket
import com.srilakshmikanthanp.clipbirdroid.types.except.NotThisPacket
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Packet Class for Invalid Packet
 */
class InvalidPacket(
  @JvmField var packetLength: Int,
  @JvmField var packetType: Int,
  @JvmField var errorCode: Int,
  @JvmField var errorMessage: ByteArray
) {
  /**
   * Allowed packet Types
   */
  enum class PacketType(val value: Int = 0x01) {
    RequestFailed(0x00),
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
    if (type != PacketType.RequestFailed.value) {
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
   * Set the Error Code
   */
  fun setErrorCode(code: Int) {
    this.errorCode = ErrorCode.fromByte(code).value
  }

  /**
   * Get the Error Code
   */
  fun getErrorCode(): Int {
    return this.errorCode
  }

  /**
   * Set the Error Message
   */
  fun setErrorMessage(message: ByteArray) {
    this.errorMessage = message
  }

  /**
   * Get the Error Message
   */
  fun getErrorMessage(): ByteArray {
    return this.errorMessage
  }

  /**
   * Size of Packet
   */
  fun size(): Int {
    return (Int.SIZE_BYTES + Byte.SIZE_BYTES + Byte.SIZE_BYTES + this.errorMessage.size)
  }

  /**
   * Convert Packet to ByteArray Big Endian
   */
  fun toByteArray(packet: InvalidPacket): ByteArray {
    // create ByteBuffer to serialize the packet
    val byteBuffer = ByteBuffer.allocate(packet.size())

    // set order
    byteBuffer.order(ByteOrder.BIG_ENDIAN)

    // put fields
    byteBuffer.putInt(packet.packetLength)
    byteBuffer.putInt(packet.packetType)
    byteBuffer.putInt(packet.errorCode)
    byteBuffer.put(packet.errorMessage)

    // return ByteArray
    return byteBuffer.array()
  }

  /**
   * Companion Object
   */
  companion object {
    /**
     * Create Packet From ByteArray Big Endian
     */
    fun fromByteArray(byteArray: ByteArray): InvalidPacket {
      // list of allowed error codes for InvalidPacket
      val allowedErrorCodes = ErrorCode.values().map { it.value }

      // create ByteBuffer to deserialize the packet
      val byteBuffer = ByteBuffer.wrap(byteArray)

      // set order
      byteBuffer.order(ByteOrder.BIG_ENDIAN)

      // get fields
      val packetLength: Int
      val packetType: Int
      val errorCode: Int
      val errorMessage: ByteArray

      // try to get bytes
      try {
        packetLength = byteBuffer.int
        packetType = byteBuffer.int
        errorCode = byteBuffer.int
        errorMessage = ByteArray(byteBuffer.remaining())
      } catch (e: BufferUnderflowException) {
        throw MalformedPacket(ErrorCode.CodingError, "BufferUnderflowException")
      }

      // msg len
      val msgLen = packetLength - (
        Int.SIZE_BYTES + Int.SIZE_BYTES + Int.SIZE_BYTES
      )

      // if not a valid message
      if (errorMessage.size != msgLen) {
        throw MalformedPacket(ErrorCode.CodingError, "Invalid Message Length")
      }

      // check the packet type
      if (packetType != PacketType.RequestFailed.value) {
        throw NotThisPacket("Not Invalid Packet")
      }

      // check the error code
      if (!allowedErrorCodes.contains(errorCode)) {
        throw MalformedPacket(ErrorCode.CodingError, "Invalid ErrorCode value: $errorCode")
      }

      // return InvalidPacket
      return InvalidPacket(packetLength, packetType, errorCode, errorMessage)
    }
  }
}
