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
  @JvmField var errorCode: ErrorCode,
  @JvmField var errorMessage: ByteArray
) {
  // Packet Fields
  private var packetLength: Int
  private var packetType: PacketType

  // init
  init {
    this.packetType = PacketType.RequestFailed
    this.packetLength = this.size()
  }

  /**
   * Allowed packet Types
   */
  enum class PacketType(val value: Int) {
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
   * Set the Error Code
   */
  fun setErrorCode(code: ErrorCode) {
    this.errorCode = code
  }

  /**
   * Get the Error Code
   */
  fun getErrorCode(): ErrorCode {
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
    return (Int.SIZE_BYTES + Int.SIZE_BYTES + Int.SIZE_BYTES + this.errorMessage.size)
  }

  /**
   * Convert Packet to ByteArray Big Endian
   */
  fun toByteArray(): ByteArray {
    // create ByteBuffer to serialize the packet
    val byteBuffer = ByteBuffer.allocate(this.size())

    // set order
    byteBuffer.order(ByteOrder.BIG_ENDIAN)

    // put fields
    byteBuffer.putInt(this.packetLength)
    byteBuffer.putInt(this.packetType.value)
    byteBuffer.putInt(this.errorCode.value)
    byteBuffer.put(this.errorMessage)

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
      val packet = InvalidPacket(ErrorCode.fromByte(errorCode), errorMessage)

      // check length
      if (packet.size() != packetLength) {
        throw MalformedPacket(ErrorCode.CodingError, "Length Does match")
      }

      // done return
      return packet
    }
  }
}
