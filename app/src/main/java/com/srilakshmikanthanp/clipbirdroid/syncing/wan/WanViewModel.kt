package com.srilakshmikanthanp.clipbirdroid.syncing.wan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.srilakshmikanthanp.clipbirdroid.Clipbird
import com.srilakshmikanthanp.clipbirdroid.common.extensions.toPem
import com.srilakshmikanthanp.clipbirdroid.common.functions.generateRSAKeyPair
import com.srilakshmikanthanp.clipbirdroid.constants.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.storage.Storage
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.device.DeviceApiRepository
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.device.DeviceRequestDto
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.device.DeviceType
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubHostDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WanViewModel @Inject constructor(
  val deviceApiRepository: DeviceApiRepository,
  val wanController: WanController,
  val storage: Storage,
  val clipbird: Clipbird
) : ViewModel() {
  private val _wanConnectionState = MutableStateFlow(WanConnectionState(isConnected = wanController.isHubConnected()))
  val wanUIState = _wanConnectionState.asStateFlow()

  init {
    viewModelScope.launch {
      wanController.hubErrorEvents.collect {
        _wanConnectionState.value = _wanConnectionState.value.copy(
          isConnected = false,
          isConnecting = false,
          error = it
        )
      }
    }

    viewModelScope.launch {
      wanController.hubConnectionStatus.collect {
        _wanConnectionState.value = _wanConnectionState.value.copy(
          isConnected = it == WanController.ConnectionEvent.CONNECTED,
          isConnecting = it == WanController.ConnectionEvent.CONNECTING,
          error = null
        )
      }
    }
  }

  fun connectToHub() {
    viewModelScope.launch {
      try {
        _wanConnectionState.value = _wanConnectionState.value.copy(isConnecting = true, error = null)
        val appServiceName = appMdnsServiceName(clipbird)
        storage.setISLastlyConnectedToHub(true)
        val device = storage.getHubHostDevice()?.let { existing ->
          val dto = DeviceRequestDto(existing.publicKey, appServiceName, existing.type)
          val updated = deviceApiRepository.updateDevice(existing.id, dto)
          existing.copy(id = updated.id, name = updated.name, type = updated.type)
        } ?: run {
          val keyPair = generateRSAKeyPair()
          val dto = DeviceRequestDto(keyPair.public.toPem(), appServiceName, DeviceType.ANDROID)
          val created = deviceApiRepository.createDevice(dto)
          HubHostDevice(created.id, created.name, created.type, created.publicKey, keyPair.private.toPem())
        }
        storage.setHubHostDevice(device)
        wanController.connectToHub(device)
      } catch (e: Exception) {
        _wanConnectionState.value = _wanConnectionState.value.copy(isConnecting = false, error = e)
      }
    }
  }

  fun disconnectFromHub() {
    storage.setISLastlyConnectedToHub(false)
    wanController.disconnectFromHub()
  }
}
