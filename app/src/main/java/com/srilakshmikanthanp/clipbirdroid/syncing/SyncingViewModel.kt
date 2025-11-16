package com.srilakshmikanthanp.clipbirdroid.syncing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.srilakshmikanthanp.clipbirdroid.ApplicationState
import com.srilakshmikanthanp.clipbirdroid.clipboard.ClipboardContent
import com.srilakshmikanthanp.clipbirdroid.syncing.manager.SyncingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncingViewModel @Inject constructor(
  val applicationState: ApplicationState,
  val syncingManager: SyncingManager,
): ViewModel() {
  fun synchronize(content: List<ClipboardContent>) {
    viewModelScope.launch { syncingManager.synchronize(content) }
  }

  fun disconnectClient(session: Session) {
    viewModelScope.launch { session.disconnect() }
  }

  fun connectToServer(server: ClientServer) {
    viewModelScope.launch { syncingManager.connectToServer(server) }
  }
}
