package com.srilakshmikanthanp.clipbirdroid.network

import com.srilakshmikanthanp.clipbirdroid.network.packets.PingPacket
import com.srilakshmikanthanp.clipbirdroid.types.enums.PingType
import org.junit.Test

class PingPacketTest {
  @Test fun pingPacketTest() {
    // construct the packets
    val packSend = PingPacket(PingType.Ping)
    val array = packSend.toByteArray()
    val packRecv = PingPacket.fromByteArray(array)

    // check
    assert(packRecv.getPacketType() == PingPacket.PacketType.Ping)
    assert(packRecv.getPingType() == PingType.Ping)
    assert(packRecv.getPacketLength() == packSend.size())
  }
}
