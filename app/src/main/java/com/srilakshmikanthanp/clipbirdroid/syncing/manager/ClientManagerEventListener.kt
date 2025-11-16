package com.srilakshmikanthanp.clipbirdroid.syncing.manager

import com.srilakshmikanthanp.clipbirdroid.syncing.ClientServer
import com.srilakshmikanthanp.clipbirdroid.syncing.Session

interface ClientManagerEventListener {
  fun onServerFound(server: ClientServer)
  fun onServerGone(server: ClientServer)
  fun onBrowsingStarted()
  fun onBrowsingStopped()
  fun onBrowsingStartFailed(e: Throwable)
  fun onBrowsingStopFailed(e: Throwable)
  fun onConnected(session: Session)
  fun onDisconnected(session: Session)
  fun onError(session: Session, e: Throwable)
}
