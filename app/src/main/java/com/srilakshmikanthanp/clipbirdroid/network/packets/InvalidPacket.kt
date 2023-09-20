package com.srilakshmikanthanp.clipbirdroid.network.packets

import com.google.protobuf.ByteString
import com.srilakshmikanthanp.clipbirdroid.types.enums.ErrorCode
import com.srilakshmikanthanp.clipbirdroid.Invalidrequest as InvalidrequestPacket

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
   * Companion Object
   */
  companion object {
    /**
     * Create Packet From ByteArray Big Endian
     */
    fun fromByteArray(byteArray: ByteArray): InvalidPacket {
      // create packet from protobuf builder
      val packet = InvalidrequestPacket.InvalidRequest.parseFrom(byteArray)

      // if any error
      if (!packet.isInitialized) {
        throw IllegalArgumentException("Invalid Packet") // TODO change exception type
      }

      // read fields
      val packetLength = packet.packetLength
      val packetType = packet.packetType
      val errorCode = packet.errorCode
      val errorMessage = packet.errorMessage.toByteArray()

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
      // create protobuf builder
      val builder = InvalidrequestPacket.InvalidRequest.newBuilder()

      // set fields
      builder.packetLength = packet.packetLength
      builder.packetType = packet.packetType
      builder.errorCode = packet.errorCode
      builder.errorMessage = ByteString.copyFrom(packet.errorMessage)

      // return ByteArray
      return builder.build().toByteArray()
    }
  }
}
