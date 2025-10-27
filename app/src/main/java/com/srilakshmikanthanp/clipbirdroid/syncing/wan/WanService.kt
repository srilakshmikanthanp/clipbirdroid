package com.srilakshmikanthanp.clipbirdroid.syncing.wan

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

@Singleton
class WanService @Inject constructor(
  private val deviceApiRepository: DeviceApiRepository,
  private val wanController: WanController,
  coroutineScope: CoroutineScope,
  private val storage: Storage,
  private val clipbird: Clipbird,
) {
  private val scope = CoroutineScope(coroutineScope.coroutineContext + SupervisorJob())

  private val _wanConnectionState = MutableStateFlow(WanConnectionState(isConnected = wanController.isHubConnected()))
  val wanConnectionState = _wanConnectionState.asStateFlow()
  private val reconnector = Reconnector()

  private inner class Reconnector {
    private val baseDelay = TimeUnit.SECONDS.toMillis(2)
    private val maxDelay = TimeUnit.MINUTES.toMillis(1)
    private val backOffFactor = 2.0
    private var attempts = 0
    private var job: Job? = null

    fun schedule() {
      if (job?.isActive == true || wanController.isHubConnected()) return
      val delay = (baseDelay * backOffFactor.pow(attempts.toDouble())).toLong().coerceAtMost(maxDelay)
      attempts++
      job = scope.launch { delay(delay).also { connectToHub() } }
    }

    fun reset() {
      job?.cancel()
      job = null
      attempts = 0
    }
  }

  init {
    scope.launch {
      wanController.hubConnectionStatus.collect {
        if (it is WanController.ConnectionEvent.DISCONNECTED && it.code != 1000) {
          reconnector.schedule()
        } else if (it is WanController.ConnectionEvent.OPENED) {
          reconnector.reset()
        }
        _wanConnectionState.value = _wanConnectionState.value.copy(
          isConnecting = it == WanController.ConnectionEvent.CONNECTING,
          isConnected = it == WanController.ConnectionEvent.CONNECTED,
          error = (it as? WanController.ConnectionEvent.DISCONNECTED)?.reason
        )
      }
    }

    scope.launch {
      wanController.hubErrorEvents.collect {
        _wanConnectionState.value = _wanConnectionState.value.copy(
          isConnected = false,
          isConnecting = false,
          error = it.localizedMessage
        )
        reconnector.schedule()
      }
    }
  }

  fun connectToHub() {
    scope.launch {
      try {
        _wanConnectionState.value = _wanConnectionState.value.copy(isConnecting = true, error = null)
        reconnector.reset()
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
        _wanConnectionState.value = _wanConnectionState.value.copy(isConnecting = false, error = e.localizedMessage)
      }
    }
  }

  fun disconnectFromHub() {
    storage.setISLastlyConnectedToHub(false)
    reconnector.reset()
    wanController.disconnectFromHub()
  }
}
