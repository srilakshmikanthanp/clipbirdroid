package com.srilakshmikanthanp.clipbirdroid.syncing

import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket

interface ClientServerSessionEventListener {
  fun onNetworkPacket(session: Session, networkPacket: NetworkPacket)
  fun onConnected(session: Session)
  fun onDisconnected(session: Session)
  fun onError(session: Session, e: Throwable)
}
