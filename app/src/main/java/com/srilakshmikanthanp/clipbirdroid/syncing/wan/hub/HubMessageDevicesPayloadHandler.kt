package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

class HubMessageDevicesPayloadHandler: HubMessagePayloadHandlerBase<HubMessageDevicesPayload> (HubMessageDevicesPayload::class.java) {
  override fun handle(hub: Hub, payload: HubMessageDevicesPayload) {
    for (device in payload.devices) {
      hub.putHubDevice(device)
    }
  }
}
