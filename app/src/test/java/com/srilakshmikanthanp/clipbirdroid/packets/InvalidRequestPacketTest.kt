package com.srilakshmikanthanp.clipbirdroid.packets

import org.junit.Test

class InvalidRequestPacketTest {
  @Test
  fun invalidRequestPacketTest() {
    val message = "Testing Packet"
    val lhs = InvalidRequestPacket(ErrorCode.CodingError, message.toByteArray())
    val array = lhs.toByteArray()
    val rhs = array.toInvalidPacket()

    assert(lhs.getPacketLength() == rhs.getPacketLength())
    assert(lhs.getPacketType() == rhs.getPacketType())
    assert(lhs.getErrorCode() == rhs.getErrorCode())
    assert(message == String(rhs.getErrorMessage()))
  }
}
