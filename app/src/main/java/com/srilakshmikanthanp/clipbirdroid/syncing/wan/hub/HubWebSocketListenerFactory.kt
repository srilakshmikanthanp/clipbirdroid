package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import dagger.assisted.AssistedFactory

@AssistedFactory
interface HubWebSocketListenerFactory {
  fun create(hubWebsocket: HubWebsocket): HubWebSocketListener
}
