package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import javax.inject.Inject

@HubMessageHandling
class HubMessageDevicesPayloadHandler @Inject constructor(): HubMessagePayloadHandlerBase<HubMessageDevicesPayload> (HubMessageDevicesPayload::class.java) {
  override fun handle(hub: Hub, payload: HubMessageDevicesPayload) {
    for (device in payload.devices) {
      hub.putHubDevice(device)
    }
  }
}
