package com.srilakshmikanthanp.clipbirdroid.mdns

interface Browser {
  fun removeListener(listener: BrowserListener)
  fun addListener(listener: BrowserListener)
  fun isBrowsing(): Boolean
  fun start()
  fun stop()
  fun restart()
}
