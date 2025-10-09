package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import dagger.assisted.AssistedFactory
import kotlinx.coroutines.CoroutineScope

@AssistedFactory
interface HubWebsocketFactory {
  fun create(device: HubHostDevice, coroutineScope: CoroutineScope,): HubWebsocket
}
