package com.srilakshmikanthanp.clipbirdroid.syncing

import com.srilakshmikanthanp.clipbirdroid.clipboard.ClipboardContent

fun interface SyncRequestHandler {
  fun onSyncRequest(items: List<ClipboardContent>)
}
