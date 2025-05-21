package com.srilakshmikanthanp.clipbirdroid.syncing.common

// Sync Request Handler
fun interface OnSyncRequestHandler {
  fun onSyncRequest(items: List<Pair<String, ByteArray>>)
}
