package com.srilakshmikanthanp.clipbirdroid.packets.exceptions

import com.srilakshmikanthanp.clipbirdroid.packets.ErrorCode

/**
 * Exception Type for Malformed packet error
 */
class MalformedPacket(val errorCode: ErrorCode, override val message: String) : Exception() {
  override fun toString(): String {
    return "MalformedPacket: $message"
  }
}
