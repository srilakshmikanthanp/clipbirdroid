package com.srilakshmikanthanp.clipbirdroid.mdns

import com.srilakshmikanthanp.clipbirdroid.common.types.Device

interface BrowserListener {
  fun onBrowsingStatusChanged(isBrowsing: Boolean)
  fun onServiceRemoved(device: Device)
  fun onServiceAdded(device: Device)
  fun onStartBrowsingFailed(errorCode: Int)
  fun onStopBrowsingFailed(errorCode: Int)
}
