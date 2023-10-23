package com.srilakshmikanthanp.clipbirdroid.network

import com.srilakshmikanthanp.clipbirdroid.network.packets.SyncingItem
import com.srilakshmikanthanp.clipbirdroid.network.packets.SyncingPacket
import org.junit.Test

class SyncingPacketTest {
  @Test fun syncingPacketTest() {
    val (mimeType, payload) = Pair("text/plain", "Hello")
    val syncingItem = SyncingItem(mimeType.toByteArray(), payload.toByteArray())
    val items = Array<SyncingItem>(5) { syncingItem }
    val packSend = SyncingPacket(5, items)
    val array = packSend.toByteArray()
    val packRecv = SyncingPacket.fromByteArray(array)

    assert(packRecv.getPacketType() == SyncingPacket.PacketType.SyncPacket)
    assert(packRecv.getPacketLength() == packSend.size())
    assert(packRecv.getItemCount() == 5)

    for (item in packRecv.getItems()) {
      assert(item.getMimeType().contentEquals(mimeType.toByteArray()))
      assert(item.getPayload().contentEquals(payload.toByteArray()))
    }
  }
}
