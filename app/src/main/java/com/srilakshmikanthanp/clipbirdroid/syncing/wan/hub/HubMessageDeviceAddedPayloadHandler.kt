package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

class HubMessageDeviceAddedPayloadHandler: HubMessagePayloadHandlerBase<HubMessageDeviceAddedPayload> (HubMessageDeviceAddedPayload::class.java) {
  override fun handle(hub: Hub, payload: HubMessageDeviceAddedPayload) {
    hub.putHubDevice(payload.device)
  }
}
