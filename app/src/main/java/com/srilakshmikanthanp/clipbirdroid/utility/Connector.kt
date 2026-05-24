package com.srilakshmikanthanp.clipbirdroid.utility

import android.util.Log
import com.srilakshmikanthanp.clipbirdroid.ApplicationState
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedServers
import com.srilakshmikanthanp.clipbirdroid.syncing.manager.ClientServerConnectionState
import com.srilakshmikanthanp.clipbirdroid.syncing.manager.SyncingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class Connector @Inject constructor(
  private val applicationState: ApplicationState,
  private val syncingManager: SyncingManager,
  private val trustedServers: TrustedServers,
  private val scope: CoroutineScope
) {
  private suspend fun tryToConnectToServer() {
    val server = syncingManager.availableServers.value.find { applicationState.getLastConnectedServer() == it.name } ?: return

    if (syncingManager.serverState.value != ClientServerConnectionState.Idle || !trustedServers.hasTrustedServer(server.name)) {
      return
    }

    for (i in 1..RETRY_COUNT) {
      try {
        syncingManager.connectToServer(server)
        return
      } catch (e: Exception) {
        Log.e("Connector", "Failed to connect to server ${server.name} on attempt $i", e)
      }

      if (i != RETRY_COUNT) {
        delay(RETRY_DELAY)
      }
    }
  }

  private val queue = Channel<Unit>(
    capacity = MAX_QUEUE_SIZE,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )

  companion object {
    private const val MAX_QUEUE_SIZE = 10
    private const val RETRY_COUNT = 3
    private const val RETRY_DELAY = 5_000L
  }

  init {
    scope.launch(Dispatchers.IO) {
      for (ignored in queue) {
        tryToConnectToServer()
      }
    }
  }

  fun enqueueConnector() {
    queue.trySend(Unit)
  }
}
