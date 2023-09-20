package com.srilakshmikanthanp.clipbirdroid.network.packets

import com.srilakshmikanthanp.clipbirdroid.types.enums.AuthStatus
import com.srilakshmikanthanp.clipbirdroid.AuthenticationOuterClass as AuthenticationPacket

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
   * Companion Object
   */
  companion object {
    /**
     * Create From byte array In BigEndian
     */
    fun fromByteArray(byteArray: ByteArray): Authentication {
      // parse the byte array with google protobuf
      val packet = AuthenticationPacket.Authentication.parseFrom(byteArray)

      // if any error
      if (!packet.isInitialized) {
        throw IllegalArgumentException("Invalid Packet") // TODO change exception type
      }

      // read fields
      val packetLength = packet.packetLength
      val packetType = packet.packetType
      val authStatus = packet.status.number

      // check packetType
      if (packetType != PacketType.AuthStatus.value) {
        throw IllegalArgumentException("Invalid PacketType value: $packetType")
      }

      // return Authentication
      return Authentication(
        packetLength,
        packetType,
        authStatus
      )
    }

    /**
     * Convert To Byte array Big Endian
     */
    fun toByteArray(auth: Authentication): ByteArray {
      // create protobuf builder
      val packet = AuthenticationPacket.Authentication.newBuilder()

      // set fields
      packet.packetLength = auth.packetLength
      packet.packetType = auth.packetType
      packet.status = AuthenticationPacket.Authentication.AuthStatus.forNumber(auth.authStatus)

      // return byte array
      return packet.build().toByteArray()
    }
  }
}
