package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import javax.inject.Inject

@HubMessageHandling
class HubMessageDeviceAddedPayloadHandler @Inject constructor(): HubMessagePayloadHandlerBase<HubMessageDeviceAddedPayload> (HubMessageDeviceAddedPayload::class.java) {
  override fun handle(hub: Hub, payload: HubMessageDeviceAddedPayload) {
    hub.putHubDevice(payload.device)
  }
}
