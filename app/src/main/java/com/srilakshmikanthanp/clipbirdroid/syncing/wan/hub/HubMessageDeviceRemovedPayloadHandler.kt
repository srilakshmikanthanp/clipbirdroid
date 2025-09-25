package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import javax.inject.Inject

@HubMessageHandling
class HubMessageDeviceRemovedPayloadHandler @Inject constructor(): HubMessagePayloadHandlerBase<HubMessageDeviceRemovedPayload> (HubMessageDeviceRemovedPayload::class.java) {
  override fun handle(hub: Hub, payload: HubMessageDeviceRemovedPayload) {
    hub.removeHubDevice(payload.device)
  }
}
