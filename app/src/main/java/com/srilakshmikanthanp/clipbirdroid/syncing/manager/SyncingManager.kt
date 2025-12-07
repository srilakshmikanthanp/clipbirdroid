package com.srilakshmikanthanp.clipbirdroid.syncing.manager

import com.srilakshmikanthanp.clipbirdroid.clipboard.ClipboardContent
import com.srilakshmikanthanp.clipbirdroid.syncing.ClientServer
import com.srilakshmikanthanp.clipbirdroid.syncing.Session
import com.srilakshmikanthanp.clipbirdroid.syncing.SyncRequestHandler
import com.srilakshmikanthanp.clipbirdroid.syncing.Synchronizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncingManager @Inject constructor(
  private val clientManager: ClientManager,
  private val serverManager: ServerManager,
  parentScope: CoroutineScope
): ClientManagerEventListener, ServerManagerEventListener, Synchronizer, SyncRequestHandler {
  private val coroutineScope = CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]))
  private val syncRequestHandlers = mutableListOf<SyncRequestHandler>()

  private val _hostManagerFlow = MutableStateFlow<HostManager?>(null)
  val hostManager: StateFlow<HostManager?> = _hostManagerFlow.asStateFlow()

  private val _availableServers: MutableStateFlow<List<ClientServer>> = MutableStateFlow(emptyList())
  val availableServers = _availableServers.asStateFlow()

  private val _connectedClients: MutableStateFlow<List<Session>> = MutableStateFlow(emptyList())
  val connectedClients = _connectedClients.asStateFlow()

  private val _connectedServer: MutableStateFlow<Session?> = MutableStateFlow(null)
  val connectedServer: StateFlow<Session?> = _connectedServer.asStateFlow()

  private val _serverFoundEvents = MutableSharedFlow<ClientServer>()
  var serverFoundEvents = _serverFoundEvents.asSharedFlow()

  override fun onServerFound(server: ClientServer) {
    _availableServers.value = _availableServers.value + server
    coroutineScope.launch { _serverFoundEvents.emit(server) }
  }

  private val _serverGoneEvents = MutableSharedFlow<ClientServer>()
  var serverGoneEvents = _serverGoneEvents.asSharedFlow()

  override fun onServerGone(server: ClientServer) {
    _availableServers.value = _availableServers.value - server
    coroutineScope.launch { _serverGoneEvents.emit(server) }
  }

  private val _isBrowsing = MutableStateFlow(false)
  val isBrowsing: StateFlow<Boolean> = _isBrowsing

  override fun onBrowsingStarted() {
    _isBrowsing.value = true
  }

  override fun onBrowsingStopped() {
    _isBrowsing.value = false
  }

  private val _browsingStartFailedEvents = MutableSharedFlow<Throwable>()
  val browsingStartFailedEvents = _browsingStartFailedEvents.asSharedFlow()

  override fun onBrowsingStartFailed(e: Throwable) {
    coroutineScope.launch { _browsingStartFailedEvents.emit(e) }
  }

  private val _browsingStopFailedEvents = MutableSharedFlow<Throwable>()
  val browsingStopFailedEvents = _browsingStopFailedEvents.asSharedFlow()

  override fun onBrowsingStopFailed(e: Throwable) {
    coroutineScope.launch { _browsingStopFailedEvents.emit(e) }
  }

  private val _connectedEvents = MutableSharedFlow<Session>()
  val connectedEvents = _connectedEvents.asSharedFlow()

  override fun onConnected(session: Session) {
    this._connectedServer.value = session
    coroutineScope.launch { _connectedEvents.emit(session) }
  }

  private val _disconnectedEvents = MutableSharedFlow<Session>()
  val disconnectedEvents = _disconnectedEvents.asSharedFlow()

  override fun onDisconnected(session: Session) {
    this._connectedServer.value = null
    coroutineScope.launch { _disconnectedEvents.emit(session) }
  }

  private val _errorEvents = MutableSharedFlow<Pair<Session, Throwable>>()
  val errorEvents = _errorEvents.asSharedFlow()

  override fun onError(session: Session, e: Throwable) {
    coroutineScope.launch { _errorEvents.emit(Pair(session, e)) }
  }

  private val _serverErrorEvents = MutableSharedFlow<Throwable>()
  val serverErrorEvents = _serverErrorEvents.asSharedFlow()

  override fun onServerError(e: Throwable) {
    coroutineScope.launch { _serverErrorEvents.emit(e) }
  }

  private val _clientDisconnectedEvents = MutableSharedFlow<Session>()
  val clientDisconnectedEvents = _clientDisconnectedEvents.asSharedFlow()

  override fun onClientDisConnected(session: Session) {
    _connectedClients.value = _connectedClients.value - session
    coroutineScope.launch { _clientDisconnectedEvents.emit(session) }
  }

  private val _clientConnectedEvents = MutableSharedFlow<Session>()
  val clientConnectedEvents = _clientConnectedEvents.asSharedFlow()

  override fun onClientConnected(session: Session) {
    _connectedClients.value = _connectedClients.value + session
    coroutineScope.launch { _clientConnectedEvents.emit(session) }
  }

  private val _serviceRegisteredEvents = MutableSharedFlow<Unit>()
  val serviceRegisteredEvents = _serviceRegisteredEvents.asSharedFlow()

  override fun onServiceRegistered() {
    coroutineScope.launch { _serviceRegisteredEvents.emit(Unit) }
  }

  private val _serviceUnregisteredEvents = MutableSharedFlow<Unit>()
  val serviceUnregisteredEvents = _serviceUnregisteredEvents.asSharedFlow()

  override fun onServiceUnregistered() {
    coroutineScope.launch { _serviceUnregisteredEvents.emit(Unit) }
  }

  private val _serviceRegisteringFailedEvents = MutableSharedFlow<Throwable>()
  val serviceRegisteringFailedEvents = _serviceRegisteringFailedEvents.asSharedFlow()

  override fun onServiceRegisteringFailed(e: Throwable) {
    coroutineScope.launch { _serviceRegisteringFailedEvents.emit(e) }
  }

  private val _serviceUnregisteringFailedEvents = MutableSharedFlow<Throwable>()
  val serviceUnregisteringFailedEvents = _serviceUnregisteringFailedEvents.asSharedFlow()

  override fun onServiceUnregisteringFailed(e: Throwable) {
    coroutineScope.launch { _serviceUnregisteringFailedEvents.emit(e) }
  }

  override fun removeSyncRequestHandler(handler: SyncRequestHandler) {
    syncRequestHandlers.remove(handler)
  }

  override fun addSyncRequestHandler(handler: SyncRequestHandler) {
    syncRequestHandlers.add(handler)
  }

  override suspend fun synchronize(items: List<ClipboardContent>) {
    hostManager.value?.synchronize(items)
  }

  override fun onSyncRequest(items: List<ClipboardContent>) {
    syncRequestHandlers.forEach { it.onSyncRequest(items) }
  }

  init {
    clientManager.addClientManagerEventListener(this)
    clientManager.addSyncRequestHandler(this)
    serverManager.addServerManagerEventListener(this)
    serverManager.addSyncRequestHandler(this)
  }

  suspend fun setHostAsServer(useBluetooth: Boolean = false) {
    this.stop()
    _hostManagerFlow.value = serverManager
    _hostManagerFlow.value?.start(useBluetooth)
  }

  suspend fun setHostAsClient(useBluetooth: Boolean = false) {
    this.stop()
    _hostManagerFlow.value  = clientManager
    _hostManagerFlow.value?.start(useBluetooth)
  }

  suspend fun stop() {
    _connectedClients.value.forEach { it.disconnect() }
    _connectedClients.value = emptyList()
    _availableServers.value = emptyList()
    _connectedServer.value?.disconnect()
    if (_hostManagerFlow.value != null) _hostManagerFlow.value?.stop()
    _hostManagerFlow.value = null
  }

  suspend fun connectToServer(server: ClientServer) {
    clientManager.connectToServer(server)
  }

  fun isConnectedToServer(): Boolean {
    return _connectedServer.value != null
  }

  fun getClientServerByName(name: String): Optional<ClientServer> {
    return Optional.ofNullable(_availableServers.value.find { it.name == name })
  }

  fun getServerClientSessionByName(name: String): Optional<Session> {
    return Optional.ofNullable(_connectedClients.value.find { it.name == name })
  }
}
