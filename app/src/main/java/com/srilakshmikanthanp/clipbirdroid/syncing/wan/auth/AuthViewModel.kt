package com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.srilakshmikanthanp.clipbirdroid.storage.Storage
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.WanController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
  private val authRepository: AuthRepository,
  private val wanController: WanController,
  private val storage: Storage
): ViewModel() {
  private val _authUIState = MutableStateFlow(AuthUIState())
  val authUIState = combine(
    _authUIState,
    storage.hubAuthTokenFlow
  ) { state, token ->
    state.copy(authToken = token)
  }.stateIn(
    viewModelScope,
    SharingStarted.Eagerly,
    AuthUIState()
  )

  fun signIn(req: BasicAuthRequestDto) {
    viewModelScope.launch {
      _authUIState.update { it.copy(isLoading = true, error = null) }
      try {
        val token = authRepository.signIn(req)
        storage.setHubAuthToken(token)
        _authUIState.update { it.copy(isLoading = false) }
      } catch (e: Exception) {
        _authUIState.update { it.copy(isLoading = false, error = e) }
      }
    }
  }

  fun signOut() {
    wanController.disconnectFromHub()
    storage.clearHubHostDevice()
    storage.clearHubAuthToken()
  }
}
