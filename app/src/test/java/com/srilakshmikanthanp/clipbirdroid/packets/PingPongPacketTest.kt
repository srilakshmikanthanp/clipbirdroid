package com.srilakshmikanthanp.clipbirdroid.packets

import org.junit.Test

class PingPongPacketTest {
  @Test
  fun pingPongPacketTest() {
    val lhs = PingPongPacket(PingPongType.Ping)
    val array = lhs.toByteArray()
    val rhs = array.toPingPongPacket()

    assert(rhs.getPacketType() == PacketType.PingPongPacket)
    assert(rhs.getPingType() == PingPongType.Ping)
    assert(rhs.getPacketLength() == lhs.getPacketLength())
  }
}
