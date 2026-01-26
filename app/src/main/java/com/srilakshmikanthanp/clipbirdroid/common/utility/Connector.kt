package com.srilakshmikanthanp.clipbirdroid.common.utility

import com.srilakshmikanthanp.clipbirdroid.ApplicationState
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedServers
import com.srilakshmikanthanp.clipbirdroid.syncing.manager.ClientServerConnectionState
import com.srilakshmikanthanp.clipbirdroid.syncing.manager.SyncingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class Connector @Inject constructor(
  private val applicationState: ApplicationState,
  private val syncingManager: SyncingManager,
  private val trustedServers: TrustedServers,
  private val scope: CoroutineScope
) {
  private val baseDelayMillis = TimeUnit.SECONDS.toMillis(5)
  private val maxDelayMillis = TimeUnit.MINUTES.toMillis(60)
  private var currentDelayMillis = baseDelayMillis

  private var job: Job? = null

  private suspend fun run() {
    syncingManager.availableServers.value.find { applicationState.getPrimaryServer() == it.name }?.let {
      if (syncingManager.serverState.value == ClientServerConnectionState.Idle && trustedServers.hasTrustedServer(it.name)) {
        try {
          syncingManager.connectToServer(it)
        } catch (e: Exception) {
          // Ignore connection failure
        }
      }
    }
  }

  fun schedule() {
    this.job = scope.launch {
      while (isActive) {
        if (syncingManager.serverState.value == ClientServerConnectionState.Idle) run()
        currentDelayMillis = (currentDelayMillis * 2).coerceAtMost(maxDelayMillis)
        delay(currentDelayMillis)
      }
    }
  }

  fun cancel() {
    this.job?.cancel()
    this.job = null
    this.currentDelayMillis = baseDelayMillis
  }

  fun reset() {
    cancel()
    schedule()
  }
}
