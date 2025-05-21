package com.srilakshmikanthanp.clipbirdroid.network

import com.srilakshmikanthanp.clipbirdroid.packets.InvalidPacket
import com.srilakshmikanthanp.clipbirdroid.common.enums.ErrorCode
import org.junit.Test

class InvalidPacketTest {
  @Test fun invalidPacketTest() {
    val messageSent = "Testing Packet"
    val packSend = InvalidPacket(ErrorCode.CodingError, messageSent.toByteArray())
    val packRecv = InvalidPacket.fromByteArray(packSend.toByteArray())

    assert(packSend.getPacketLength() == packRecv.getPacketLength())
    assert(packSend.getPacketType() == packRecv.getPacketType())
    assert(packSend.getErrorCode() == packRecv.getErrorCode())

    val messageRecv = String(packRecv.getErrorMessage())

    assert(messageRecv == messageSent)
  }
}
