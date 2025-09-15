package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

class HubMessageDeviceUpdatedPayloadHandler: HubMessagePayloadHandlerBase<HubMessageDeviceUpdatedPayload> (HubMessageDeviceUpdatedPayload::class.java) {
  override fun handle(hub: Hub, payload: HubMessageDeviceUpdatedPayload) {
    hub.putHubDevice(payload.device)
  }
}
