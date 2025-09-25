package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import com.srilakshmikanthanp.clipbirdroid.common.functions.decrypt
import javax.inject.Inject

@HubMessageHandling
class HubMessageClipboardDispatchPayloadHandler @Inject constructor(): HubMessagePayloadHandlerBase<HubMessageClipboardDispatchPayload> (HubMessageClipboardDispatchPayload::class.java) {
  override fun handle(hub: Hub, payload: HubMessageClipboardDispatchPayload) {
    val items = payload.clipboard.map { Pair(it.first, decrypt(it.second, hub.getHubHostDevice().privateKey.toByteArray())) }
    for (handler in hub.getSyncRequestHandlers()) {
      handler.onSyncRequest(items)
    }
  }
}
