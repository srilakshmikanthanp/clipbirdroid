package com.srilakshmikanthanp.clipbirdroid.syncing.manager

import com.srilakshmikanthanp.clipbirdroid.syncing.Session

interface ServerManagerEventListener {
  fun onClientDisConnected(session: Session)
  fun onClientConnected(session: Session)
  fun onServiceRegistered()
  fun onServiceUnregistered()
  fun onServiceRegisteringFailed(e: Throwable)
  fun onServiceUnregisteringFailed(e: Throwable)
  fun onError(session: Session, e: Throwable)
}
