package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

interface HubListener {
  fun onErrorOccurred(throwable: Throwable)
  fun onConnected()
  fun onConnecting()
  fun onDisconnected(code: Int, reason: String)
}
