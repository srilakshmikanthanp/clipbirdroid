package com.srilakshmikanthanp.clipbirdroid.packets

/**
 * Exception Type for Not This Packet error
 */
class NotThisPacketException(override val message: String) : Exception(message) {
  override fun toString(): String {
    return "NotThisPacket: $message"
  }
}
