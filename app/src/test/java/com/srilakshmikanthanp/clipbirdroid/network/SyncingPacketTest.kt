package com.srilakshmikanthanp.clipbirdroid.network

import com.srilakshmikanthanp.clipbirdroid.packets.SyncingItem
import com.srilakshmikanthanp.clipbirdroid.packets.SyncingPacket
import org.junit.Test

class SyncingPacketTest {
  @Test fun syncingPacketTest() {
    val (mimeType, payload) = Pair("text/plain", "Hello")
    val syncingItem = com.srilakshmikanthanp.clipbirdroid.packets.SyncingItem(
        mimeType.toByteArray(),
        payload.toByteArray()
    )
    val items = Array<com.srilakshmikanthanp.clipbirdroid.packets.SyncingItem>(5) { syncingItem }
    val packSend = com.srilakshmikanthanp.clipbirdroid.packets.SyncingPacket(items)
    val array = packSend.toByteArray()
    val packRecv = com.srilakshmikanthanp.clipbirdroid.packets.SyncingPacket.fromByteArray(array)

    assert(packRecv.getPacketType() == com.srilakshmikanthanp.clipbirdroid.packets.SyncingPacket.PacketType.SyncPacket)
    assert(packRecv.getPacketLength() == packSend.size())
    assert(packRecv.getItemCount() == 5)

    for (item in packRecv.getItems()) {
      assert(item.getMimeType().contentEquals(mimeType.toByteArray()))
      assert(item.getPayload().contentEquals(payload.toByteArray()))
    }
  }
}
