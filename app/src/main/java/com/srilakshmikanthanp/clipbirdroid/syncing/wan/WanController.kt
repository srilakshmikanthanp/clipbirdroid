package com.srilakshmikanthanp.clipbirdroid.syncing.wan

import com.srilakshmikanthanp.clipbirdroid.controller.Controller
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth.SessionManager
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
  coroutineScope: CoroutineScope,
  private val sessionManager: SessionManager
): HubListener, Controller {
  private val _syncRequestEvents = MutableSharedFlow<List<Pair<String, ByteArray>>>()
  val syncRequestEvents: SharedFlow<List<Pair<String, ByteArray>>> = _syncRequestEvents.asSharedFlow()

  private val _hubErrorEvents = MutableSharedFlow<Throwable>()
  val hubErrorEvents: SharedFlow<Throwable> = _hubErrorEvents.asSharedFlow()

  private val _hubConnectionEvents = MutableSharedFlow<ConnectionEvent>()
  val hubConnectionStatus: SharedFlow<ConnectionEvent> = _hubConnectionEvents.asSharedFlow()

  private val scope = CoroutineScope(coroutineScope.coroutineContext + SupervisorJob())
  private var hub: Optional<HubWebsocket> = Optional.empty()

  private fun notifySyncRequest(data: List<Pair<String, ByteArray>>) {
    this.scope.launch {
      this@WanController._syncRequestEvents.emit(data)
    }
  }

  enum class ConnectionEvent {
    DISCONNECTED,
    CONNECTED,
    CONNECTING
  }

  override fun onErrorOccurred(throwable: Throwable) {
    this.scope.launch {
      this@WanController._hubErrorEvents.emit(throwable)
    }
  }

  override fun onConnected() {
    this.scope.launch {
      this@WanController._hubConnectionEvents.emit(ConnectionEvent.CONNECTED)
    }
  }

  override fun onConnecting() {
    this.scope.launch {
      this@WanController._hubConnectionEvents.emit(ConnectionEvent.CONNECTING)
    }
  }

  override fun onDisconnected(code: Int, reason: String) {
    this.scope.launch {
      this@WanController._hubConnectionEvents.emit(ConnectionEvent.DISCONNECTED)
    }
  }

  init {
    this.scope.launch {
      sessionManager.tokenFlow.collect { if (it == null && hub.isPresent) disconnectFromHub() }
    }
  }

  fun connectToHub(device: HubHostDevice) {
    if (hub.isPresent && hub.get().isConnected()) throw RuntimeException("Hub is already connected")
    val hubWebsocket = hubWebsocketFactory.create(device, this.scope)
    hubWebsocket.addHubListener(this)
    hubWebsocket.addSyncRequestHandler(::notifySyncRequest)
    this.hub = Optional.of(hubWebsocket)
    hubWebsocket.connect()
  }

  fun isHubConnected(): Boolean {
    return hub.map { it.isConnected() }.orElse(false)
  }

  fun disconnectFromHub() {
    if (hub.isEmpty) throw RuntimeException("Hub is not connected")
    hub.get().disconnect()
  }

  fun isHubAvailable(): Boolean {
    return hub.isPresent
  }

  fun reconnectToHub() {
    if (hub.isEmpty) throw RuntimeException("Hub is not connected before")
    if (hub.get().isConnected()) throw RuntimeException("Hub is already connected")
    hub.get().connect()
  }

  fun synchronize(data: List<Pair<String, ByteArray>>) {
    hub.ifPresent { it.synchronize(data) }
  }
}
