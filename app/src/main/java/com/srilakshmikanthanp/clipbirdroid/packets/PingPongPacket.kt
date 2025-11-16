package com.srilakshmikanthanp.clipbirdroid.packets

import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder


class PingPongPacket (private var pingPongType: PingPongType): NetworkPacket {
  private var packetType: PacketType = PacketType.PingPongPacket

  fun getPacketLength(): Int {
    return (Int.SIZE_BYTES + Int.SIZE_BYTES + Int.SIZE_BYTES)
  }

  fun getPacketType(): PacketType {
    return this.packetType
  }

  fun setPingType(type: PingPongType) {
    this.pingPongType = type
  }

  fun getPingType(): PingPongType {
    return this.pingPongType
  }

  override fun toByteArray(): ByteArray {
    val byteBuffer = ByteBuffer.allocate(this.getPacketLength())
    byteBuffer.order(ByteOrder.BIG_ENDIAN)
    byteBuffer.putInt(this.getPacketLength())
    byteBuffer.putInt(this.packetType.code)
    byteBuffer.putInt(this.pingPongType.value)
    return byteBuffer.array()
  }
}

enum class PingPongType (val value: Int = 0x00) {
  Ping(0x00), Pong(0x01);

  fun toByte(status: PingPongType): Int {
    return status.value
  }

  companion object {
    fun fromInt(value: Int): PingPongType = when (value) {
      Ping.value -> Ping
      Pong.value -> Pong
      else -> throw IllegalArgumentException("Invalid PingType value: $value")
    }
  }
}

fun ByteArray.toPingPongPacket(): PingPongPacket {
  val byteBuffer = ByteBuffer.wrap(this)
  byteBuffer.order(ByteOrder.BIG_ENDIAN)

  val packetLength: Int
  val packetType: Int
  val pingType: Int

  try {
    packetLength = byteBuffer.int
    packetType = byteBuffer.int
  } catch (e: BufferUnderflowException) {
    throw MalformedPacketException(ErrorCode.CodingError, "Invalid Packet Length")
  }

  if (packetType != PacketType.PingPongPacket.code) {
    throw NotThisPacketException("Not a PingPacket")
  }

  try {
    pingType = byteBuffer.int
  } catch (e: BufferUnderflowException) {
    throw MalformedPacketException(ErrorCode.CodingError, "Invalid Ping Type")
  }

  if (pingType !in PingPongType.entries.map { it.value }) {
    throw MalformedPacketException(ErrorCode.CodingError, "Invalid Ping Type")
  }

  val packet =  PingPongPacket(PingPongType.fromInt(pingType))

  if (packetLength != packet.getPacketLength()) {
    throw MalformedPacketException(ErrorCode.CodingError, "Invalid Packet Length")
  }

  return packet
}
