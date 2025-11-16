package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

interface BtBrowser {
  fun removeListener(listener: BtBrowserListener)
  fun addListener(listener: BtBrowserListener)
  fun start()
  fun stop()
}
