package com.srilakshmikanthanp.clipbirdroid.packets

import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AuthenticationPacket(private var authenticationStatus: AuthenticationStatus): NetworkPacket {
  private val packetType: PacketType = PacketType.AuthenticationPacket

  fun getPacketLength(): Int {
    return (Int.SIZE_BYTES + Int.SIZE_BYTES + Int.SIZE_BYTES)
  }

  fun getPacketType(): PacketType {
    return this.packetType
  }

  fun setAuthStatus(status: AuthenticationStatus) {
    this.authenticationStatus = status
  }

  fun getAuthStatus(): AuthenticationStatus {
    return this.authenticationStatus
  }

  override fun toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(this.getPacketLength())
    buffer.order(ByteOrder.BIG_ENDIAN)
    buffer.putInt(this.getPacketLength())
    buffer.putInt(this.packetType.code)
    buffer.putInt(this.authenticationStatus.value)
    return buffer.array()
  }
}

enum class AuthenticationStatus(val value: Int = 0x00) {
  AuthOkay(0x00), AuthFail(0x01);

  companion object {
    fun fromInt(value: Int): AuthenticationStatus = when (value) {
      AuthOkay.value -> AuthOkay
      AuthFail.value -> AuthFail
      else -> throw IllegalArgumentException("Invalid AuthStatus value: $value")
    }
  }
}

fun ByteArray.toAuthenticationPacket(): AuthenticationPacket {
  val allowedAuthenticationStatus = AuthenticationStatus.entries.map { it.value }
  val buffer = ByteBuffer.wrap(this)

  buffer.order(ByteOrder.BIG_ENDIAN)

  val packetLength: Int
  val packetType: Int
  val authStatus: Int

  try {
    packetLength = buffer.int
    packetType = buffer.int
  } catch (e: BufferUnderflowException) {
    throw MalformedPacketException(ErrorCode.CodingError, "Invalid Packet Length")
  }

  if (packetType != PacketType.AuthenticationPacket.code) {
    throw NotThisPacketException("Not Authentication Packet")
  }

  try {
    authStatus = buffer.int
  } catch (e: BufferUnderflowException) {
    throw MalformedPacketException(ErrorCode.CodingError, "Invalid Packet Length")
  }

  if (!allowedAuthenticationStatus.contains(authStatus)) {
    throw MalformedPacketException(ErrorCode.CodingError, "Invalid AuthStatus value")
  }

  val packet = AuthenticationPacket(AuthenticationStatus.fromInt(authStatus))

  if (packetLength != packet.getPacketLength()) {
    throw MalformedPacketException(ErrorCode.CodingError, "Invalid Packet Length")
  }

  return packet
}
