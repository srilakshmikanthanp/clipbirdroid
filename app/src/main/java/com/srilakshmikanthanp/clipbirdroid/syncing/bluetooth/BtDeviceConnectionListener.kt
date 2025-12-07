package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

interface BtDeviceConnectionListener {
  fun onDeviceConnected(device: BtResolvedDevice)
  fun onDeviceDisconnected(device: BtResolvedDevice)
}
