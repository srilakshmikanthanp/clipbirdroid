package com.srilakshmikanthanp.clipbirdroid.network

import com.srilakshmikanthanp.clipbirdroid.network.packets.InvalidPacket
import com.srilakshmikanthanp.clipbirdroid.types.enums.ErrorCode
import org.junit.Test

class InvalidPacketTest {
  @Test
  fun invalidPacketTest() {
    val packSend = InvalidPacket(ErrorCode.CodingError, "Testing Packet".toByteArray())
    val array = packSend.toByteArray()
    val packRecv = InvalidPacket.fromByteArray(array)

    assert(packSend.getPacketLength() == packRecv.getPacketLength())
    assert(packSend.getPacketType() == packRecv.getPacketType())
    assert(packSend.getErrorCode() == packRecv.getErrorCode())

    val messageRecv = String(packRecv.getErrorMessage())
    val messageSent = String(packRecv.getErrorMessage())

    assert(messageRecv == messageSent)
  }
}
