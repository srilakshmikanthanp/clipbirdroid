package com.srilakshmikanthanp.clipbirdroid.syncing.network

interface NetBrowserListener {
  fun onServiceRemoved(device: NetResolvedDevice)
  fun onServiceAdded(device: NetResolvedDevice)
  fun onBrowsingStopped()
  fun onBrowsingStarted()
  fun onStartBrowsingFailed(errorCode: Int)
  fun onStopBrowsingFailed(errorCode: Int)
}
