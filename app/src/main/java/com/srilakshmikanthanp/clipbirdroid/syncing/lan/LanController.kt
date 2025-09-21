package com.srilakshmikanthanp.clipbirdroid.syncing.lan

import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.common.enums.HostType
import com.srilakshmikanthanp.clipbirdroid.common.types.Device
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.controller.Controller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LanController(
    private val sslConfig: SSLConfig,
    private val context: Context,
    coroutineScope: CoroutineScope
): Controller {
  //----------------------- client Signals -------------------------//
  private val _servers = MutableStateFlow<Set<Device>>(emptySet())
  val servers = _servers.asStateFlow()

  private val _serverFoundEvents = MutableSharedFlow<Device>()
  val serverFoundEvents: SharedFlow<Device> = _serverFoundEvents.asSharedFlow()

  private val _serverGoneEvents = MutableSharedFlow<Device>()
  val serverGoneEvents: SharedFlow<Device> = _serverGoneEvents.asSharedFlow()

  private val _connectionErrors = MutableSharedFlow<String>()
  val connectionErrors: SharedFlow<String> = _connectionErrors.asSharedFlow()

  private val _serverStatusEvents = MutableSharedFlow<Pair<Boolean, Device>>()
  val serverStatusEvents = _serverStatusEvents.asSharedFlow()

  private val _browsingStatusEvents = MutableSharedFlow<Boolean>()
  val browsingStatusEvents = _browsingStatusEvents.asSharedFlow()

  private val _browsingStartFailedEvents = MutableSharedFlow<Int>()
  val browsingStartFailedEvents: SharedFlow<Int> = _browsingStartFailedEvents

  private val _browsingStopFailedEvents = MutableSharedFlow<Int>()
  val browsingStopFailedEvents: SharedFlow<Int> = _browsingStopFailedEvents

  //----------------------- server Signals ------------------------//
  private val _clientStateChangedEvents = MutableSharedFlow<Pair<Device, Boolean>>()
  val clientStateEvents: SharedFlow<Pair<Device, Boolean>> = _clientStateChangedEvents.asSharedFlow()

  private val _mdnsRegisterStatusEvents = MutableSharedFlow<Boolean>()
  val mdnsRegisterStatusEvents: SharedFlow<Boolean> = _mdnsRegisterStatusEvents.asSharedFlow()

  private val _mdnsServiceRegisterFailedEvents = MutableSharedFlow<Int>()
  val mdnsServiceRegisterFailedEvents: SharedFlow<Int> = _mdnsServiceRegisterFailedEvents.asSharedFlow()

  private val _mdnsServiceUnregisterFailedEvents = MutableSharedFlow<Int>()
  val mdnsServiceUnregisterFailedEvents: SharedFlow<Int> = _mdnsServiceUnregisterFailedEvents.asSharedFlow()

  private val _authRequestEvents = MutableSharedFlow<Device>()
  val authRequestEvents: SharedFlow<Device> = _authRequestEvents.asSharedFlow()

  private val _clients = MutableStateFlow<List<Device>>(emptyList())
  val clients = _clients.asStateFlow()

  private val _syncRequestEvents = MutableSharedFlow<List<Pair<String, ByteArray>>>()
  val syncRequestEvents: SharedFlow<List<Pair<String, ByteArray>>> = _syncRequestEvents.asSharedFlow()

  private val _hostTypeChangeEvent = MutableSharedFlow<HostType>()
  val hostTypeChangeEvent: SharedFlow<HostType> = _hostTypeChangeEvent

  //------------------------- Member Variables -------------------------//

  private val scope = CoroutineScope(coroutineScope.coroutineContext + SupervisorJob())
  private var host: Host? = null

  //----------------------- private notifiers ------------------------//

  /**
   * Notify the server list changed (Client)
   */
  private fun notifyServerListChanged(servers: Set<Device>) {
    this._servers.value = servers
  }

  /**
   * Notify the server Found (Client)
   */
  private fun notifyServerFound(server: Device) {
    this.scope.launch {
      this@LanController._serverFoundEvents.emit(server)
    }
  }

  /**
   * Notify the server gone (Client)
   */
  private fun notifyServerGone(server: Device) {
    this.scope.launch {
      this@LanController._serverGoneEvents.emit(server)
    }
  }

  /**
   * Notify the connection error (Client)
   */
  private fun notifyConnectionError(error: String) {
    this.scope.launch {
      this@LanController._connectionErrors.emit(error)
    }
  }

  /**
   * Notify the server status changed (Client)
   */
  private fun notifyServerStatusChanged(status: Boolean, server: Device) {
    this.scope.launch {
      this@LanController._serverStatusEvents.emit(Pair(status, server))
    }
  }

  /**
   * Notify the client state changed (Server)
   */
  private fun notifyClientStateChanged(client: Device, connected: Boolean) {
    this.scope.launch {
      this@LanController._clientStateChangedEvents.emit(Pair(client, connected))
    }
  }

  /**
   * Notify the server state changed (Server)
   */
  private fun notifyServerStateChanged(status: Boolean) {
    this.scope.launch {
      this@LanController._mdnsRegisterStatusEvents.emit(status)
    }
  }

  /**
   * Notify the auth request (Server)
   */
  private fun notifyAuthRequest(client: Device) {
    this.scope.launch {
      this@LanController._authRequestEvents.emit(client)
    }
  }

  /**
   * Notify the client list changed (Server)
   */
  private fun notifyClientListChanged(clients: List<Device>) {
    this._clients.value = clients
  }

  /**
   * Notify the sync request (Common)
   */
  private fun notifySyncRequest(data: List<Pair<String, ByteArray>>) {
    this.scope.launch {
      this@LanController._syncRequestEvents.emit(data)
    }
  }

  /**
   * Notify the host type changed (Common)
   */
  private fun notifyHostTypeChanged(host: HostType) {
    this.scope.launch {
      this@LanController._hostTypeChangeEvent.emit(host)
    }
  }

  private fun notifyBrowsingStatusChanged(isBrowsing: Boolean) {
    this.scope.launch {
      this@LanController._browsingStatusEvents.emit(isBrowsing)
    }
  }

  private fun notifyBrowsingStartFailed(error: Int) {
    this.scope.launch {
      this@LanController._browsingStartFailedEvents.emit(error)
    }
  }

  private fun notifyBrowsingStopFailed(error: Int) {
    this.scope.launch {
      this@LanController._browsingStopFailedEvents.emit(error)
    }
  }

  private fun notifyMdnsServiceRegisterFailed(error: Int) {
    this.scope.launch {
      this@LanController._mdnsServiceRegisterFailedEvents.emit(error)
    }
  }

  private fun notifyMdnsServiceUnregisterFailed(error: Int) {
    this.scope.launch {
      this@LanController._mdnsServiceUnregisterFailedEvents.emit(error)
    }
  }

  /**
   * Set Current Host As Server
   */
  fun setAsServer() {
    if (host != null) this.destroyHost()
    val server = Server(context)
    server.setSslConfig(sslConfig)
    server.addMdnsServiceUnregisterFailedHandler(::notifyMdnsServiceUnregisterFailed)
    server.addMdnsRegisterStatusChangeHandler(::notifyServerStateChanged)
    server.addClientStateChangeHandler(::notifyClientStateChanged)
    server.addAuthRequestHandler(::notifyAuthRequest)
    server.addSyncRequestHandler(::notifySyncRequest)
    server.addClientListChangeHandler(::notifyClientListChanged)
    server.addMdnsServiceRegisterFailedHandler(::notifyMdnsServiceRegisterFailed)
    server.startServer()
    this.host = server
    notifyHostTypeChanged(HostType.SERVER)
  }

  /**
   * Set Current Host As Client
   */
  fun setAsClient() {
    if (host != null) this.destroyHost()
    val client = Client(context)
    client.setSslConfig(sslConfig)
    client.addServerStatusChangeHandler(::notifyServerStatusChanged)
    client.addBrowsingStatusChangeHandler(::notifyBrowsingStatusChanged)
    client.addServerGoneHandler(::notifyServerGone)
    client.addServerListChangeHandler(::notifyServerListChanged)
    client.addServerFoundHandler(::notifyServerFound)
    client.addSyncRequestHandler(::notifySyncRequest)
    client.addConnectionErrorHandler(::notifyConnectionError)
    client.addBrowsingStartFailedHandler(::notifyBrowsingStartFailed)
    client.addBrowsingStopFailedHandler(::notifyBrowsingStopFailed)
    client.startBrowsing()
    this.host = client
    notifyHostTypeChanged(HostType.CLIENT)
  }

  fun onClientAuthenticated(client: Device) {
    val server = this.host as? Server ?: throw RuntimeException("Host is not server")
    server.onClientAuthenticated(client)
  }

  fun onClientNotAuthenticated(client: Device) {
    val server = this.host as? Server ?: throw RuntimeException("Host is not server")
    server.onClientNotAuthenticated(client)
  }

  fun destroyHost() {
    val host = this.host ?: return

    if (host is Server) {
      host.removeMdnsServiceUnregisterFailedHandler(::notifyMdnsServiceUnregisterFailed)
      host.removeMdnsRegisterStatusChangeHandler(::notifyServerStateChanged)
      host.removeClientStateChangeHandler(::notifyClientStateChanged)
      host.removeAuthRequestHandler(::notifyAuthRequest)
      host.removeSyncRequestHandler(::notifySyncRequest)
      host.removeClientListChangeHandler(::notifyClientListChanged)
      host.removeMdnsServiceRegisterFailedHandler(::notifyMdnsServiceRegisterFailed)
      host.stopServer()
    }

    if (host is Client) {
      if (host.getConnectedServer() != null) host.disconnectFromServer()
      host.removeBrowsingStatusChangeHandler(::notifyBrowsingStatusChanged)
      host.removeServerStatusChangeHandler(::notifyServerStatusChanged)
      host.removeServerFoundHandler(::notifyServerFound)
      host.removeServerListChangeHandler(::notifyServerListChanged)
      host.removeServerGoneHandler(::notifyServerGone)
      host.removeSyncRequestHandler(::notifySyncRequest)
      host.removeConnectionErrorHandler(::notifyConnectionError)
      host.removeBrowsingStartFailedHandler(::notifyBrowsingStartFailed)
      host.removeBrowsingStopFailedHandler(::notifyBrowsingStopFailed)
      host.stopBrowsing()
    }

    this.host = null
  }

  fun getHostAsServerOrThrow(): Server {
    return host as? Server ?: throw RuntimeException("Host is not server")
  }

  fun getHostAsClientOrThrow(): Client {
    return host as? Client ?: throw RuntimeException("Host is not client")
  }

  fun getHostType(): HostType {
    return when (host) {
      is Server -> HostType.SERVER
      is Client -> HostType.CLIENT
      else -> HostType.NONE
    }
  }

  fun getHost(): Host? {
    return host
  }

  fun synchronize(data: List<Pair<String, ByteArray>>) {
    host?.synchronize(data)
  }
}
