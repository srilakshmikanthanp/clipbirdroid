package com.srilakshmikanthanp.clipbirdroid.types.except

import com.srilakshmikanthanp.clipbirdroid.types.enums.ErrorCode

/**
 * Exception Type for Malformed packet error
 */
class MalformedPacket(val errorCode: ErrorCode, override val message: String) : Exception() {
  override fun toString(): String {
    return "MalformedPacket: $message"
  }
}
