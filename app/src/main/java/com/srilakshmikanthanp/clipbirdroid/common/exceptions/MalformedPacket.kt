package com.srilakshmikanthanp.clipbirdroid.common.exceptions

import com.srilakshmikanthanp.clipbirdroid.common.enums.ErrorCode

/**
 * Exception Type for Malformed packet error
 */
class MalformedPacket(val errorCode: ErrorCode, override val message: String) : Exception() {
  override fun toString(): String {
    return "MalformedPacket: $message"
  }
}
