package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import javax.inject.Inject

@HubMessageHandling
class HubMessageNonceChallengeCompletedPayloadHandler @Inject constructor(): HubMessagePayloadHandlerBase<HubMessageNonceChallengeCompletedPayload> (HubMessageNonceChallengeCompletedPayload::class.java) {
  override fun handle(hub: Hub, payload: HubMessageNonceChallengeCompletedPayload) {
    hub.getListeners().forEach { it.onConnected() }
  }
}
