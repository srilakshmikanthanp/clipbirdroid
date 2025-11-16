package com.srilakshmikanthanp.clipbirdroid.packets

import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class InvalidRequestPacket(private var errorCode: ErrorCode, private var errorMessage: ByteArray): NetworkPacket {
  private val packetType: PacketType = PacketType.InvalidRequest

  fun getPacketLength(): Int {
    return (Int.SIZE_BYTES + Int.SIZE_BYTES + Int.SIZE_BYTES + this.errorMessage.size)
  }

  fun getPacketType(): PacketType {
    return this.packetType
  }

  fun setErrorCode(code: ErrorCode) {
    this.errorCode = code
  }

  fun getErrorCode(): ErrorCode {
    return this.errorCode
  }

  fun setErrorMessage(message: ByteArray) {
    this.errorMessage = message
  }

  fun getErrorMessage(): ByteArray {
    return this.errorMessage
  }

  override fun toByteArray(): ByteArray {
    val byteBuffer = ByteBuffer.allocate(this.getPacketLength())
    byteBuffer.order(ByteOrder.BIG_ENDIAN)
    byteBuffer.putInt(this.getPacketLength())
    byteBuffer.putInt(this.packetType.code)
    byteBuffer.putInt(this.errorCode.value)
    byteBuffer.put(this.errorMessage)
    return byteBuffer.array()
  }
}

enum class ErrorCode(val value: Int = 0x00) {
  CodingError(0x00), InvalidPacket(0x01);

  companion object {
    fun fromInt(value: Int): ErrorCode = when (value) {
      CodingError.value -> CodingError
      InvalidPacket.value -> InvalidPacket
      else -> throw IllegalArgumentException("Invalid ErrorCode value: $value")
    }
  }
}

fun ByteArray.toInvalidPacket(): InvalidRequestPacket {
  val allowedErrorCodes = ErrorCode.entries.map { it.value }
  val byteBuffer = ByteBuffer.wrap(this)
  byteBuffer.order(ByteOrder.BIG_ENDIAN)

  val packetLength: Int
  val packetType: Int
  val errorCode: Int
  val errorMessage: ByteArray

  try {
    packetLength = byteBuffer.int
    packetType = byteBuffer.int
  } catch (e: BufferUnderflowException) {
    throw MalformedPacketException(ErrorCode.CodingError, "Invalid Packet Length")
  }

  if (packetType != PacketType.InvalidRequest.code) {
    throw NotThisPacketException("Not Invalid Packet")
  }

  try {
    errorCode = byteBuffer.int
    errorMessage = ByteArray(byteBuffer.remaining())
    byteBuffer.get(errorMessage)
  } catch (e: BufferUnderflowException) {
    throw MalformedPacketException(ErrorCode.CodingError, "Invalid Packet Length")
  }

  if (errorMessage.size != packetLength - (Int.SIZE_BYTES * 3)) {
    throw MalformedPacketException(ErrorCode.CodingError, "Invalid Message Length")
  }

  if (!allowedErrorCodes.contains(errorCode)) {
    throw MalformedPacketException(ErrorCode.CodingError, "Invalid ErrorCode value")
  }

  return InvalidRequestPacket(ErrorCode.fromInt(errorCode), errorMessage)
}
