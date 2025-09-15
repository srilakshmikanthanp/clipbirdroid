package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

class HubMessageNonceChallengeCompletedPayloadHandler: HubMessagePayloadHandlerBase<HubMessageNonceChallengeCompletedPayload> (HubMessageNonceChallengeCompletedPayload::class.java) {
  override fun handle(hub: Hub, payload: HubMessageNonceChallengeCompletedPayload) {
    hub.getListeners().forEach { it.onConnected() }
  }
}
