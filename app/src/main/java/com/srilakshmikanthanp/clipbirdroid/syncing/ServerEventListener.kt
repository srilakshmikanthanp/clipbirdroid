package com.srilakshmikanthanp.clipbirdroid.syncing

import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket

interface ServerEventListener {
  fun onClientDisconnected(client: Session)
  fun onClientConnected(client: Session)
  fun onNetworkPacket(client: Session, networkPacket: NetworkPacket)
  fun onClientError(client: Session, e: Throwable)
  fun onServiceRegistered()
  fun onServiceUnregistered()
  fun onServiceRegistrationFailed(e: Throwable)
  fun onServiceUnregistrationFailed(e: Throwable)
}
