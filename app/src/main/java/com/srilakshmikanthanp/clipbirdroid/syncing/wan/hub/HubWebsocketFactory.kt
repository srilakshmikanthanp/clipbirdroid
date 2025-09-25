package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import dagger.assisted.AssistedFactory

@AssistedFactory
interface HubWebsocketFactory {
  fun create(device: HubHostDevice): HubWebsocket
}
