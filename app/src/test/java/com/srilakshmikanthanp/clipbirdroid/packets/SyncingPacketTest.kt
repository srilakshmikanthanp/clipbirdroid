package com.srilakshmikanthanp.clipbirdroid.packets

import org.junit.Test

class SyncingPacketTest {
  @Test
  fun syncingPacketTest() {
    val syncingItem = SyncingItem("text/plain".toByteArray(), "Hello".toByteArray())
    val items = Array(5) { syncingItem }
    val lhs = SyncingPacket(items)
    val array = lhs.toByteArray()
    val rhs = array.toSyncingPacket()

    assert(rhs.getPacketType() == PacketType.SyncingPacket)
    assert(rhs.getPacketLength() == lhs.getPacketLength())
    assert(rhs.getItemCount() == 5)

    for (item in rhs.getItems()) {
      assert(item.getMimeType().contentEquals(syncingItem.getMimeType()))
      assert(item.getPayload().contentEquals(syncingItem.getPayload()))
    }
  }
}
