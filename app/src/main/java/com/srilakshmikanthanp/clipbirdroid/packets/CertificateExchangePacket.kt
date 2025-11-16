package com.srilakshmikanthanp.clipbirdroid.packets

import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CertificateExchangePacket(private var certificateBytes: ByteArray): NetworkPacket {
  private var packetType: PacketType = PacketType.CertificateExchange

  fun getPacketLength(): Int {
    return Int.SIZE_BYTES + Int.SIZE_BYTES + certificateBytes.size
  }

  fun getPacketType(): PacketType {
    return this.packetType
  }

  fun getCertificate(): ByteArray {
    return this.certificateBytes
  }

  fun setCertificate(bytes: ByteArray) {
    this.certificateBytes = bytes
  }

  override fun toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(this.getPacketLength())
    buffer.order(ByteOrder.BIG_ENDIAN)
    buffer.putInt(this.getPacketLength())
    buffer.putInt(this.packetType.code)
    buffer.put(this.certificateBytes)
    return buffer.array()
  }
}

fun ByteArray.toCertificateExchangePacket(): CertificateExchangePacket {
  val buffer = ByteBuffer.wrap(this)

  buffer.order(ByteOrder.BIG_ENDIAN)

  val packetLength: Int
  val packetType: Int

  try {
    packetLength = buffer.int
    packetType = buffer.int
  } catch (e: BufferUnderflowException) {
    throw MalformedPacketException(ErrorCode.InvalidPacket, "Incomplete Certificate Exchange Packet")
  }

  if (packetType != PacketType.CertificateExchange.code) {
    throw NotThisPacketException("Not a Certificate Exchange Packet")
  }

  val certificateBytes: ByteArray = ByteArray(packetLength - Int.SIZE_BYTES - Int.SIZE_BYTES)

  try {
    buffer.get(certificateBytes)
  } catch (e: BufferUnderflowException) {
    throw MalformedPacketException(ErrorCode.InvalidPacket, "Incomplete Certificate Exchange Packet")
  }

  return CertificateExchangePacket(certificateBytes)
}
