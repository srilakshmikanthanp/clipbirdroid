package com.srilakshmikanthanp.clipbirdroid.syncing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.srilakshmikanthanp.clipbirdroid.ApplicationState
import com.srilakshmikanthanp.clipbirdroid.clipboard.ClipboardContent
import com.srilakshmikanthanp.clipbirdroid.syncing.manager.SyncingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

  private val _errorEvents = MutableSharedFlow<String>()
  val errorEvents = _errorEvents.asSharedFlow()

  fun connectToServer(server: ClientServer) {
    viewModelScope.launch {
      try {
        syncingManager.connectToServer(server)
      } catch (e: Exception) {
        _errorEvents.emit("Failed to connect to server ${server.name}: ${e.message}")
      }
    }
  }
}
