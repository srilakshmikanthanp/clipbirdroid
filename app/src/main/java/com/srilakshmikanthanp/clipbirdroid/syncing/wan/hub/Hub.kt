package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import com.srilakshmikanthanp.clipbirdroid.syncing.SyncRequestHandler
import com.srilakshmikanthanp.clipbirdroid.syncing.Synchronizer
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.device.DeviceResponseDto

interface Hub : Synchronizer {
  fun getSyncRequestHandlers(): List<SyncRequestHandler>
  fun getHubHostDevice(): HubHostDevice
  fun getHubDevices(): Set<DeviceResponseDto>
  fun removeHubDevice(device: DeviceResponseDto)
  fun putHubDevice(device: DeviceResponseDto)
  fun sendMessage(message: HubMessage<*>)
  fun addHubListener(listener: HubListener)
  fun getListeners(): List<HubListener>
}
