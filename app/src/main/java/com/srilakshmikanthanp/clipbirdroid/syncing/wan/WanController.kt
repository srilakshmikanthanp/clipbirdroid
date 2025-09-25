package com.srilakshmikanthanp.clipbirdroid.syncing.wan

import com.srilakshmikanthanp.clipbirdroid.controller.Controller
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubHostDevice
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubListener
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubWebsocket
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubWebsocketFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WanController @Inject constructor(
  private val hubWebsocketFactory: HubWebsocketFactory,
  coroutineScope: CoroutineScope
): HubListener, Controller {
  private val _syncRequestEvents = MutableSharedFlow<List<Pair<String, ByteArray>>>()
  val syncRequestEvents: SharedFlow<List<Pair<String, ByteArray>>> = _syncRequestEvents.asSharedFlow()

  private val _hubErrorEvents = MutableSharedFlow<Throwable>()
  val hubErrorEvents: SharedFlow<Throwable> = _hubErrorEvents.asSharedFlow()

  private val _hubConnectionEvents = MutableSharedFlow<Boolean>()
  val hubConnectionStatus: SharedFlow<Boolean> = _hubConnectionEvents.asSharedFlow()

  private val scope = CoroutineScope(coroutineScope.coroutineContext + SupervisorJob())
  private var hub: Optional<HubWebsocket> = Optional.empty()

  private fun notifySyncRequest(data: List<Pair<String, ByteArray>>) {
    this.scope.launch {
      this@WanController._syncRequestEvents.emit(data)
    }
  }

  override fun onErrorOccurred(throwable: Throwable) {
    this.hub = Optional.empty()
    this.scope.launch {
      this@WanController._hubErrorEvents.emit(throwable)
    }
  }

  override fun onConnected() {
    this.scope.launch {
      this@WanController._hubConnectionEvents.emit(true)
    }
  }

  override fun onDisconnected() {
    this.hub = Optional.empty()
    this.scope.launch {
      this@WanController._hubConnectionEvents.emit(false)
    }
  }

  fun connectToHub(device: HubHostDevice) {
    if (hub.isPresent) throw RuntimeException("Hub is already connected")
    val hubWebsocket = hubWebsocketFactory.create(device)
    hubWebsocket.addHubListener(this)
    hubWebsocket.addSyncRequestHandler(::notifySyncRequest)
    this.hub = Optional.of(hubWebsocket)
    hubWebsocket.connect()
  }

  fun synchronize(data: List<Pair<String, ByteArray>>) {
    hub.ifPresent { it.synchronize(data) }
  }

  fun isHubConnected(): Boolean {
    return hub.isPresent
  }

  fun disconnectFromHub() {
    if (hub.isEmpty) throw RuntimeException("Hub is not connected")
    hub.get().disconnect()
    hub = Optional.empty()
  }
}
