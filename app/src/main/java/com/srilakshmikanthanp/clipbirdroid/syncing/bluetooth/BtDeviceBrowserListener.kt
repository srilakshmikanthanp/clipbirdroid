package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

interface BtDeviceBrowserListener {
  fun onDeviceFound(device: BtResolvedDevice)
  fun onDeviceGone(device: BtResolvedDevice)
}
