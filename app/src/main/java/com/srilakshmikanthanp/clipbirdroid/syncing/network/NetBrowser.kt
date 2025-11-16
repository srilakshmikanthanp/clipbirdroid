package com.srilakshmikanthanp.clipbirdroid.syncing.network

interface NetBrowser {
  fun removeListener(listener: NetBrowserListener)
  fun addListener(listener: NetBrowserListener)
  fun start()
  fun stop()
}
