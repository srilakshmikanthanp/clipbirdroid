package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket

interface BtConnectionListener {
  fun onHandShakeCompleted(btConnection: BtConnection)
  fun onDisconnected(btConnection: BtConnection)
  fun onError(btConnection: BtConnection, cause: Throwable)
  fun onNetworkPacket(btConnection: BtConnection, packet: NetworkPacket)
}
