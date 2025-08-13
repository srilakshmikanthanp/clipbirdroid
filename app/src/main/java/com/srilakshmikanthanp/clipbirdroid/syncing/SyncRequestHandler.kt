package com.srilakshmikanthanp.clipbirdroid.syncing

// Sync Request Handler
fun interface SyncRequestHandler {
  fun onSyncRequest(items: List<Pair<String, ByteArray>>)
}
