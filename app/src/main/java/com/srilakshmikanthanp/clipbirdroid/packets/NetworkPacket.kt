package com.srilakshmikanthanp.clipbirdroid.packets

import java.nio.ByteBuffer

sealed interface NetworkPacket {
  fun toByteArray(): ByteArray
}

fun ByteBuffer.toNetworkPacket(): NetworkPacket {
  try {
    return this.array().toCertificateExchangePacket()
  } catch (e: NotThisPacketException) {}

  try {
    return this.array().toInvalidPacket()
  } catch (e: NotThisPacketException) {}

  try {
    return this.array().toAuthenticationPacket()
  } catch (e: NotThisPacketException) {}

  try {
    return this.array().toPingPongPacket()
  } catch (e: NotThisPacketException) {}

  try {
    return this.array().toSyncingPacket()
  } catch (e: NotThisPacketException) {}

  throw UnknownPacketException("Unable to decode the packet")
}

