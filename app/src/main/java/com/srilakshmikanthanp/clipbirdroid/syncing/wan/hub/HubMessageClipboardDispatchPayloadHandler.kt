package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import com.srilakshmikanthanp.clipbirdroid.common.functions.decrypt

class HubMessageClipboardDispatchPayloadHandler: HubMessagePayloadHandlerBase<HubMessageClipboardDispatchPayload> (HubMessageClipboardDispatchPayload::class.java) {
  override fun handle(hub: Hub, payload: HubMessageClipboardDispatchPayload) {
    val items = payload.clipboard.map { Pair(it.first, decrypt(it.second, hub.getHubHostDevice().publicKey.toByteArray())) }
    for (handler in hub.getSyncRequestHandlers()) {
      handler.onSyncRequest(items)
    }
  }
}
