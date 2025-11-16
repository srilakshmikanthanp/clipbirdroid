package com.srilakshmikanthanp.clipbirdroid.packets

class MalformedPacketException(val errorCode: ErrorCode, override val message: String) : Exception() {
  override fun toString(): String {
    return "MalformedPacket: $message"
  }
}
