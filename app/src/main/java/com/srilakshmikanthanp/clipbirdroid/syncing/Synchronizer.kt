package com.srilakshmikanthanp.clipbirdroid.syncing

import com.srilakshmikanthanp.clipbirdroid.clipboard.ClipboardContent
import java.util.concurrent.Future

interface Synchronizer {
  fun removeSyncRequestHandler(handler: SyncRequestHandler)
  fun addSyncRequestHandler(handler: SyncRequestHandler)
  suspend fun synchronize(items: List<ClipboardContent>)
}
