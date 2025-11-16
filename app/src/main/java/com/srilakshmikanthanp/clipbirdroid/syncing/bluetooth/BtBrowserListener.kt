package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

interface BtBrowserListener {
  fun onServiceRemoved(device: BtResolvedDevice)
  fun onServiceAdded(device: BtResolvedDevice)
  fun onBrowsingStopped()
  fun onBrowsingStarted()
}
