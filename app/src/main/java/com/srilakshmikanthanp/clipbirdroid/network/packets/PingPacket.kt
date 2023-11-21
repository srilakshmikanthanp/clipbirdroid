package com.srilakshmikanthanp.clipbirdroid.network.packets

import com.srilakshmikanthanp.clipbirdroid.types.enums.ErrorCode
import com.srilakshmikanthanp.clipbirdroid.types.enums.PingType
import com.srilakshmikanthanp.clipbirdroid.types.except.MalformedPacket
import com.srilakshmikanthanp.clipbirdroid.types.except.NotThisPacket
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * Packet Class for Ping Packet
 */
class PingPacket (private var pingType: PingType) {
  // Packet Fields
  private var packetLength: Int
  private var packetType: PacketType

  // init
  init {
    this.packetType = PacketType.Ping
    this.packetLength = this.size()
  }

  /**
   * Allowed packet Types
   */
  enum class PacketType(val value: Int) {
    Ping(0x03),
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
   * Set the Ping Type
   */
  fun setPingType(type: PingType) {
    this.pingType = type
  }

  /**
   * Get the Ping Type
   */
  fun getPingType(): PingType {
    return this.pingType
  }

  /**
   * Size of Packet
   */
  fun size(): Int {
    return (Int.SIZE_BYTES + Int.SIZE_BYTES + Int.SIZE_BYTES)
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
    byteBuffer.putInt(this.pingType.value)

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
    fun fromByteArray(byteArray: ByteArray): PingPacket {
      // create ByteBuffer to deserialize the packet
      val byteBuffer = ByteBuffer.wrap(byteArray)

      // set order
      byteBuffer.order(ByteOrder.BIG_ENDIAN)

      // get fields
      val packetLength: Int
      val packetType: Int
      val pingType: Int

      try {
        packetLength = byteBuffer.int
        packetType = byteBuffer.int
      } catch (e: BufferUnderflowException) {
        throw MalformedPacket(ErrorCode.CodingError, "Invalid Packet Length")
      }

      // check packet type
      if (packetType != PacketType.Ping.value) {
        throw NotThisPacket("Not a PingPacket")
      }

      // get ping type
      try {
        pingType = byteBuffer.int
      } catch (e: BufferUnderflowException) {
        throw MalformedPacket(ErrorCode.CodingError, "Invalid Ping Type")
      }

      // check ping type
      if (pingType !in PingType.values().map { it.value }) {
        throw MalformedPacket(ErrorCode.CodingError, "Invalid Ping Type")
      }

      // return the packet
      return PingPacket(PingType.fromInt(pingType))
    }
  }
}
