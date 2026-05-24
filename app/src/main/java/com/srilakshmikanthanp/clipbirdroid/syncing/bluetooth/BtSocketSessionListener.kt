package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket

interface BtSocketSessionListener {
  fun onHandShakeCompleted(btSocketSession: BtSocketSession)
  fun onDisconnected(btSocketSession: BtSocketSession)
  fun onError(btSocketSession: BtSocketSession, cause: Throwable)
  fun onNetworkPacket(btSocketSession: BtSocketSession, packet: NetworkPacket)
}
