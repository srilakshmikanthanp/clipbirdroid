package com.srilakshmikanthanp.clipbirdroid.syncing

interface ClientServerBrowserEventListener {
  fun onServerFound(server: ClientServer)
  fun onServerGone(server: ClientServer)
  fun onBrowsingStarted()
  fun onBrowsingStopped()
  fun onBrowsingStartFailed(e: Throwable)
  fun onBrowsingStopFailed(e: Throwable)
}
