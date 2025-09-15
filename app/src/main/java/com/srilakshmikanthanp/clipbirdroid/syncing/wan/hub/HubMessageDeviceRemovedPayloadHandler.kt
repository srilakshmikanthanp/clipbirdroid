package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

class HubMessageDeviceRemovedPayloadHandler: HubMessagePayloadHandlerBase<HubMessageDeviceRemovedPayload> (HubMessageDeviceRemovedPayload::class.java) {
  override fun handle(hub: Hub, payload: HubMessageDeviceRemovedPayload) {
    hub.removeHubDevice(payload.device)
  }
}
