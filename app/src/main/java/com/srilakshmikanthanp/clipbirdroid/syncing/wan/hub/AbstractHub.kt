package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import com.srilakshmikanthanp.clipbirdroid.common.functions.encrypt
import com.srilakshmikanthanp.clipbirdroid.syncing.SyncRequestHandler
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.device.DeviceResponseDto

abstract class AbstractHub(private val hubHostDevice: HubHostDevice ): Hub {
  private val syncRequestHandlers = mutableListOf<SyncRequestHandler>()
  private val devices = mutableSetOf<DeviceResponseDto>()
  private val listeners = mutableListOf<HubListener>()

  override fun getSyncRequestHandlers(): List<SyncRequestHandler> {
    return syncRequestHandlers
  }

  override fun addSyncRequestHandler(handler: SyncRequestHandler) {
    this.syncRequestHandlers.add(handler)
  }

  override fun removeSyncRequestHandler(handler: SyncRequestHandler) {
    this.syncRequestHandlers.remove(handler)
  }

  override fun getHubHostDevice(): HubHostDevice {
    return hubHostDevice
  }

  override fun getHubDevices(): Set<DeviceResponseDto> {
    return devices.toSet()
  }

  override fun removeHubDevice(device: DeviceResponseDto) {
    devices.removeIf { it.id == device.id }
  }

  override fun putHubDevice(device: DeviceResponseDto) {
    devices.removeIf { it.id == device.id }
    devices.add(device)
  }

  override fun addHubListener(listener: HubListener) {
    listeners.add(listener)
  }

  override fun getListeners(): List<HubListener> {
    return listeners.toList()
  }

  override fun synchronize(items: List<Pair<String, ByteArray>>) {
    for (device in devices) {
      val encryptedItems = items.map { Pair(it.first, encrypt(it.second, device.publicKey.toByteArray())) }
      val payload = HubMessageClipboardForwardPayload(toDeviceId = device.id, clipboard = encryptedItems)
      val message = HubMessage(type = HubMessageType.CLIPBOARD_FORWARD, payload = payload)
      this.sendMessage(message)
    }
  }
}
