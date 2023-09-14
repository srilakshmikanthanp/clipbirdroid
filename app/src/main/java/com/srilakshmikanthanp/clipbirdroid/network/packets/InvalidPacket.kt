package com.srilakshmikanthanp.clipbirdroid.network.packets

import com.srilakshmikanthanp.clipbirdroid.types.enums.ErrorCode
import java.nio.ByteBuffer

/**
 * Packet Class for Invalid Packet
 */
class InvalidPacket(
  @JvmField var packetLength: Int,
  @JvmField var packetType: Byte,
  @JvmField var errorCode: Byte,
  @JvmField var errorMessage: ByteArray
) {
  /**
   * Allowed packet Types
   */
  enum class PacketType(val value: Byte = 0x01) {
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
  fun setPacketType(type: Byte) {
    // check packetType
    if (type != PacketType.RequestFailed.value) {
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
   * Set the Error Code
   */
  fun setErrorCode(code: Byte) {
    this.errorCode = ErrorCode.fromByte(code).value
  }

  /**
   * Get the Error Code
   */
  fun getErrorCode(): Byte {
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
   * Companion Object
   */
  companion object {
    /**
     * Create Packet From ByteArray Big Endian
     */
    fun fromByteArray(byteArray: ByteArray): InvalidPacket {
      // create ByteBuffer from byte array
      val buffer = ByteBuffer.wrap(byteArray)

      // read fields
      val packetLength = buffer.int
      val packetType = buffer.get()
      val errorCode = buffer.get()
      val errorMessage = ByteArray(buffer.remaining())
      buffer.get(errorMessage)

      // check packetType
      if (packetType != PacketType.RequestFailed.value) {
        throw IllegalArgumentException("Invalid PacketType value: $packetType")
      }

      // return InvalidPacket
      return InvalidPacket(
        packetLength,
        packetType,
        errorCode,
        errorMessage
      )
    }

    /**
     * Convert Packet to ByteArray Big Endian
     */
    fun toByteArray(packet: InvalidPacket): ByteArray {
      // create ByteBuffer
      val buffer = ByteBuffer.allocate(packet.size())

      // write fields
      buffer.putInt(packet.packetLength)
      buffer.put(packet.packetType)
      buffer.put(packet.errorCode)
      buffer.put(packet.errorMessage)

      // return ByteArray
      return buffer.array()
    }
  }
}
