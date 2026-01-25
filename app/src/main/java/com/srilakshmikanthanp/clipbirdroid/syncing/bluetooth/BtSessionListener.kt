package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket

interface BtSessionListener {
  fun onHandShakeCompleted(btSession: BtSession)
  fun onDisconnected(btSession: BtSession)
  fun onError(btSession: BtSession, cause: Throwable)
  fun onNetworkPacket(btSession: BtSession, packet: NetworkPacket)
}
