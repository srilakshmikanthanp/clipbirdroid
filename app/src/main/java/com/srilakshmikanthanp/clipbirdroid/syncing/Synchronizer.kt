package com.srilakshmikanthanp.clipbirdroid.syncing

interface Synchronizer {
  fun addSyncRequestHandler(handler: SyncRequestHandler)
  fun removeSyncRequestHandler(handler: SyncRequestHandler)
  fun synchronize(items: List<Pair<String, ByteArray>>)
}
