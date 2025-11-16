package com.srilakshmikanthanp.clipbirdroid.syncing.manager

import com.srilakshmikanthanp.clipbirdroid.syncing.Synchronizer

sealed interface HostManager: Synchronizer {
  suspend fun start(useBluetooth: Boolean)
  suspend fun stop()
}
