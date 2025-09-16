package com.srilakshmikanthanp.clipbirdroid.controller

import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.clipboard.Clipboard
import com.srilakshmikanthanp.clipbirdroid.common.enums.HostType
import com.srilakshmikanthanp.clipbirdroid.common.types.Device
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.common.variant.Variant
import com.srilakshmikanthanp.clipbirdroid.constants.appMaxHistory
import com.srilakshmikanthanp.clipbirdroid.store.Storage
import com.srilakshmikanthanp.clipbirdroid.syncing.lan.client.Client
import com.srilakshmikanthanp.clipbirdroid.syncing.lan.server.Server
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppController(private val sslConfig: SSLConfig, private val context: Context): AutoCloseable {
  //----------------------- client Signals -------------------------//
  private val _servers = MutableStateFlow<Set<Device>>(emptySet())
  val servers = _servers.asStateFlow()

  private val _serverFoundEvents = MutableSharedFlow<Device>()
  val serverFoundEvents: SharedFlow<Device> = _serverFoundEvents.asSharedFlow()

  private val _serverGoneEvents = MutableSharedFlow<Device>()
  val serverGoneEvents: SharedFlow<Device> = _serverGoneEvents.asSharedFlow()

  private val _connectionErrors = MutableSharedFlow<String>()
  val connectionErrors: SharedFlow<String> = _connectionErrors.asSharedFlow()

  private val _serverStatus = MutableSharedFlow<Pair<Boolean, Device>>()
  val serverStatus = _serverStatus.asSharedFlow()

  private val _browsingStatus = MutableSharedFlow<Boolean>()
  val browsingStatus = _browsingStatus.asSharedFlow()

  private val _browsingStartFailedEvents = MutableSharedFlow<Int>()
  val browsingStartFailedEvents: SharedFlow<Int> = _browsingStartFailedEvents

  private val _browsingStopFailedEvents = MutableSharedFlow<Int>()
  val browsingStopFailedEvents: SharedFlow<Int> = _browsingStopFailedEvents

  //----------------------- server Signals ------------------------//
  private val _clientStateChangedEvents = MutableSharedFlow<Pair<Device, Boolean>>()
  val clientStateChangedEvents: SharedFlow<Pair<Device, Boolean>> = _clientStateChangedEvents.asSharedFlow()

  private val _mdnsRegisterStatusEvents = MutableSharedFlow<Boolean>()
  val mdnsRegisterStatusEvents: SharedFlow<Boolean> = _mdnsRegisterStatusEvents.asSharedFlow()

  private val _mdnsServiceRegisterFailedEvents = MutableSharedFlow<Int>()
  val mdnsServiceRegisterFailedEvents: SharedFlow<Int> = _mdnsServiceRegisterFailedEvents.asSharedFlow()

  private val _mdnsServiceUnregisterFailedEvents = MutableSharedFlow<Int>()
  val mdnsServiceUnregisterFailedEvents: SharedFlow<Int> = _mdnsServiceUnregisterFailedEvents.asSharedFlow()

  private val _authRequest = MutableSharedFlow<Device>()
  val authRequest: SharedFlow<Device> = _authRequest.asSharedFlow()

  private val _clients = MutableStateFlow<List<Device>>(emptyList())
  val clients = _clients.asStateFlow()

  //----------------------- Common Signals ------------------------//

  private val _syncRequests = MutableSharedFlow<List<Pair<String, ByteArray>>>()
  val syncRequests: SharedFlow<List<Pair<String, ByteArray>>> = _syncRequests.asSharedFlow()

  private val _hostTypeChangeEvent = MutableSharedFlow<HostType>()
  val hostTypeChangeEvent: SharedFlow<HostType> = _hostTypeChangeEvent

  //----------------------- Helper Functions ---------------------------//

  private fun destroyHost() {
    if (host.holds(Server::class.java)) {
      // get the server and disconnect the signals
      val server : Server = host.get() as Server

      // set null
      server.stopServer().also { host.set(null) }

      // disconnect the signals from Server
      server.removeMdnsRegisterStatusChangeHandler(::notifyServerStateChanged)
      server.removeClientStateChangeHandler(::handleClientStateChanged)
      server.removeClientStateChangeHandler(::notifyClientStateChanged)
      server.removeAuthRequestHandler(::notifyAuthRequest)
      server.removeSyncRequestHandler(::notifySyncRequest)
      server.removeSyncRequestHandler(::handleSyncRequest)
      server.removeSyncRequestHandler(clipboard::setClipboardContent)
      server.removeClientListChangeHandler(::notifyClientListChanged)
      server.removeMdnsServiceRegisterFailedHandler(::notifyMdnsServiceRegisterFailed)
      server.removeMdnsServiceUnregisterFailedHandler(::notifyMdnsServiceUnregisterFailed)

      // Disconnect the signals to Server
      clipboard.removeClipboardChangeListener(server::synchronize)
    }

    if (host.holds(Client::class.java)) {
      // get the client and disconnect the signals
      val client : Client = host.get() as Client

      // set null
      client.stopBrowsing().also { host.set(null) }

      // if connected to server then disconnect
      if (client.getConnectedServer() != null) {
        client.disconnectFromServer()
      }

      // disconnect the signals from Client
      client.removeServerStatusChangeHandler(::handleServerStatusChanged)
      client.removeServerStatusChangeHandler(::notifyServerStatusChanged)
      client.removeServerFoundHandler(::handleServerFound)
      client.removeServerFoundHandler(::notifyServerFound)
      client.removeServerListChangeHandler(::notifyServerListChanged)
      client.removeServerGoneHandler(::notifyServerGone)
      client.removeSyncRequestHandler(::notifySyncRequest)
      client.removeSyncRequestHandler(::handleSyncRequest)
      client.removeSyncRequestHandler(clipboard::setClipboardContent)
      client.removeConnectionErrorHandler(::notifyConnectionError)
      client.removeBrowsingStatusChangeHandler(::notifyBrowsingStatusChanged)
      client.removeBrowsingStartFailedHandler(::notifyBrowsingStartFailed)
      client.removeBrowsingStopFailedHandler(::notifyBrowsingStopFailed)

      // Disconnect the signals to Client
      clipboard.removeClipboardChangeListener(client::synchronize)
    }
  }

  //------------------------- Member Variables -------------------------//

  private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
  private val storage: Storage = Storage.getInstance(context)
  private val host: Variant = Variant()
  private val clipboard: Clipboard = Clipboard(context)
  private val TAG = "AppController"

  private val _history = MutableStateFlow(emptyList<List<Pair<String, ByteArray>>>().toMutableList())
  val history = _history.asStateFlow()

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
      this@AppController._serverFoundEvents.emit(server)
    }
  }

  /**
   * Notify the server gone (Client)
   */
  private fun notifyServerGone(server: Device) {
    this.scope.launch {
      this@AppController._serverGoneEvents.emit(server)
    }
  }

  /**
   * Notify the connection error (Client)
   */
  private fun notifyConnectionError(error: String) {
    this.scope.launch {
      this@AppController._connectionErrors.emit(error)
    }
  }

  /**
   * Notify the server status changed (Client)
   */
  private fun notifyServerStatusChanged(status: Boolean, server: Device) {
    this.scope.launch {
      this@AppController._serverStatus.emit(Pair(status, server))
    }
  }

  /**
   * Notify the client state changed (Server)
   */
  private fun notifyClientStateChanged(client: Device, connected: Boolean) {
    this.scope.launch {
      this@AppController._clientStateChangedEvents.emit(Pair(client, connected))
    }
  }

  /**
   * Notify the server state changed (Server)
   */
  private fun notifyServerStateChanged(status: Boolean) {
    this.scope.launch {
      this@AppController._mdnsRegisterStatusEvents.emit(status)
    }
  }

  /**
   * Notify the auth request (Server)
   */
  private fun notifyAuthRequest(client: Device) {
    this.scope.launch {
      this@AppController._authRequest.emit(client)
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
      this@AppController._syncRequests.emit(data)
    }
  }

  /**
   * Notify the host type changed (Common)
   */
  private fun notifyHostTypeChanged(host: HostType) {
    this.scope.launch {
      this@AppController._hostTypeChangeEvent.emit(host)
    }
  }

  private fun notifyBrowsingStatusChanged(isBrowsing: Boolean) {
    this.scope.launch {
      this@AppController._browsingStatus.emit(isBrowsing)
    }
  }

  private fun notifyBrowsingStartFailed(error: Int) {
    this.scope.launch {
      this@AppController._browsingStartFailedEvents.emit(error)
    }
  }

  private fun notifyBrowsingStopFailed(error: Int) {
    this.scope.launch {
      this@AppController._browsingStopFailedEvents.emit(error)
    }
  }

  private fun notifyMdnsServiceRegisterFailed(error: Int) {
    this.scope.launch {
      this@AppController._mdnsServiceRegisterFailedEvents.emit(error)
    }
  }

  private fun notifyMdnsServiceUnregisterFailed(error: Int) {
    this.scope.launch {
      this@AppController._mdnsServiceUnregisterFailedEvents.emit(error)
    }
  }

  //------------------------- private slots -------------------------//

  /**
   * Handle the Client State Changes (From server)
   */
  private fun handleClientStateChanged(client: Device, connected: Boolean) {
    // if the host is not server then throw error
    if (!this.host.holds(Server::class.java)) {
      throw RuntimeException("Host is not server")
    }

    // if not Connected the return
    if (!connected) return

    // Get the Host as Server
    val server: Server = this.host.get() as Server

    // get the device certificate from the server
    val cert = server.getClientCertificate(client)

    // store the client certificate
    storage.setClientCert(client.name, cert)
  }

  /**
   * Handle the Server Found (From client)
   */
  private fun handleServerFound(server: Device) {
    // if the host is not client then throw error
    if (!host.holds(Client::class.java)) {
      throw RuntimeException("Host is not client")
    }

    // get the client and store the server
    val client = host.get() as Client

    // if already connected then return
    if (client.getConnectedServer() != null) return

    // if the server is not found then return
    if (storage.hasServerCert(server.name)) {
      client.connectToServerSecured(server)
    }
  }

  /**
   * Handle the Server Status Changes (From client)
   */
  private fun handleServerStatusChanged(status: Boolean, srv: Device) {
    // if the host is not client then throw error
    if (!host.holds(Client::class.java)) {
      throw RuntimeException("Host is not client")
    }

    // get the client and disconnect the signals
    val client = host.get() as Client

    // if the client is connected then connect the signals
    if (status) {
      clipboard.addClipboardChangeListener(client::synchronize)
      val cert = client.getConnectedServerCertificate()
      val name = srv.name
      storage.setServerCert(name, cert)
      return
    }

    // Remove the clipboard change listener
    clipboard.removeClipboardChangeListener(client::synchronize)

    // get all server
    for (s in client.getServerList()) {
      if (s != srv && storage.hasServerCert(s.name)) {
        return client.connectToServerSecured(s)
      }
    }
  }

  /**
   * Handle Sync Request
   */
  private fun handleSyncRequest(clip: List<Pair<String, ByteArray>>) {
    if (_history.value.size + 1 > appMaxHistory()) {
      val newClipHist = _history.value.toMutableList()
      newClipHist.removeAt(newClipHist.lastIndex)
      _history.value = newClipHist
    }

    val newClipHist = _history.value.toMutableList()
    newClipHist.add(0, clip)
    _history.value = newClipHist
  }

  //------------------------- public slots -------------------------//

  /**
   * Set Current Host As Server
   */
  fun setCurrentHostAsServer() {
    // destroy the host if already exists
    if (host.hasObject()) this.destroyHost()

    // create the server object with context
    val server: Server = host.set(Server(context)) as Server

    // set the ssl configuration
    server.setSslConfig(sslConfig)

    // connect the client state changed signal
    server.addClientStateChangeHandler(::handleClientStateChanged)
    server.addClientStateChangeHandler(::notifyClientStateChanged)

    // connect the OnClipboardChange signal to the server
    clipboard.addClipboardChangeListener(server::synchronize)

    // connect the auth request signal
    server.addAuthRequestHandler(::notifyAuthRequest)

    // connect the sync request signal
    server.addSyncRequestHandler(::notifySyncRequest)
    server.addSyncRequestHandler(::handleSyncRequest)
    server.addSyncRequestHandler(clipboard::setClipboardContent)

    // connect the client list changed signal
    server.addClientListChangeHandler(::notifyClientListChanged)

    // connect the server state changed signal
    server.addMdnsRegisterStatusChangeHandler(::notifyServerStateChanged)

    server.addMdnsServiceRegisterFailedHandler(::notifyMdnsServiceRegisterFailed)
    server.addMdnsServiceUnregisterFailedHandler(::notifyMdnsServiceUnregisterFailed)

    // set the host is server
    storage.setHostIsLastlyServer(true)

    // Start the server to listen and accept the client
    server.startServer()

    // notify the host type changed
    notifyHostTypeChanged(HostType.SERVER)
  }

  /**
   * Set Current Host As Client
   */
  fun setCurrentHostAsClient() {
    // destroy the host if already exists
    if (host.hasObject()) this.destroyHost()

    // Create the Client object with context
    val client: Client = host.set(Client(context)) as Client

    // Set the SSL Configuration
    client.setSslConfig(sslConfig)

    // Connect the server status changed signal
    client.addServerStatusChangeHandler(::handleServerStatusChanged)
    client.addServerStatusChangeHandler(::notifyServerStatusChanged)

    // Connect the server gone signal
    client.addServerGoneHandler(::notifyServerGone)

    // Connect the server list changed signal
    client.addServerListChangeHandler(::notifyServerListChanged)

    // Connect the server found signal
    client.addServerFoundHandler(::handleServerFound)
    client.addServerFoundHandler(::notifyServerFound)

    // Connect the sync request signal
    client.addSyncRequestHandler(::notifySyncRequest)
    client.addSyncRequestHandler(::handleSyncRequest)
    client.addSyncRequestHandler(clipboard::setClipboardContent)

    // Connect the connection error signal
    client.addConnectionErrorHandler(::notifyConnectionError)

    // Connect the clipboard change signal
    clipboard.addClipboardChangeListener(client::synchronize)

    // Connect the browsing status change signal
    client.addBrowsingStatusChangeHandler(::notifyBrowsingStatusChanged)

    client.addBrowsingStartFailedHandler(::notifyBrowsingStartFailed)

    client.addBrowsingStopFailedHandler(::notifyBrowsingStopFailed)

    // set the host is client
    storage.setHostIsLastlyServer(false)

    // Start the discovery
    client.startBrowsing()

    // notify the host type changed
    notifyHostTypeChanged(HostType.CLIENT)
  }

  //------------------- Store functions ------------------------//

  /**
   * Clear Server Certificates
   */
  fun clearServerCertificates() {
    storage.clearAllServerCert()
  }

  /**
   * Clear Client Certificates
   */
  fun clearClientCertificates() {
    storage.clearAllClientCert()
  }

  //------------------- Server functions ------------------------//

  /**
   * Get the Clients that are connected to the server
   */
  fun getConnectedClientsList(): List<Device> {
    // if the host is not server then throw
    if (!host.holds(Server::class.java)) {
      throw RuntimeException("Host is not server")
    }

    // get the server
    val server = host.get() as Server

    // return the clients
    return server.getClients()
  }

  /**
   * Disconnect the client from the server and delete
   * the client
   */
  fun disconnectClient(client: Device) {
    // find the client with the given host and ip
    val match = { i: Device -> i.ip == client.ip && i.port == client.port }

    // if the host is not server then throw
    if (!host.holds(Server::class.java)) {
      throw RuntimeException("Host is not server")
    }

    // get the list of clients
    val clients = getConnectedClientsList()

    // find the client
    clients.find(match).also {
      if (it == null) throw RuntimeException("Client not found")
    }

    // get the Server
    val server = host.get() as Server

    // disconnect the client
    server.disconnectClient(client)
  }

  /**
   * Disconnect the all the clients from the server
   */
  fun disconnectAllClients() {
    // if the host is not server then throw
    if (!host.holds(Server::class.java)) {
      throw RuntimeException("Host is not server")
    }

    // get the server
    val server = host.get() as Server

    // disconnect all the clients
    server.disconnectAllClients()
  }

  /**
   * Is Server Started
   */
  fun isServerStarted(): Boolean {
    // if the host is not server then throw
    if (!host.holds(Server::class.java)) {
      throw RuntimeException("Host is not server")
    }

    // get the server
    val server = host.get() as Server

    // return the server status
    return server.isRunning()
  }

  fun isServerRegistered(): Boolean {
    // if the host is not server then throw
    if (!host.holds(Server::class.java)) {
      throw RuntimeException("Host is not server")
    }

    // get the server
    val server = host.get() as Server

    // return the server registered status
    return server.isRegistered()
  }

  /**
   * Get the server QHostAddress and port
   */
  fun getServerInfo(): Device {
    // if the host is not server then throw
    if (!host.holds(Server::class.java)) {
      throw RuntimeException("Host is not server")
    }

    // get the server
    val server = host.get() as Server

    // return the server address and port
    return server.getServerInfo()
  }

  /**
   * The function that is called when the client is authenticated
   */
  fun onClientAuthenticated(client: Device) {
    // if the host is not server then throw
    if (!host.holds(Server::class.java)) {
      throw RuntimeException("Host is not server")
    }

    // get the server
    val server = host.get() as Server

    // call the server function
    server.onClientAuthenticated(client)
  }

  /**
   * The function that is called when the client it not
   * authenticated
   */
  fun onClientNotAuthenticated(client: Device) {
    // if the host is not server then throw
    if (!host.holds(Server::class.java)) {
      throw RuntimeException("Host is not server")
    }

    // get the server
    val server = host.get() as Server

    // call the server function
    server.onClientNotAuthenticated(client)
  }

  /**
   * Dispose the Server
   */
  fun disposeServer() {
    // if the host is not server then throw
    if (!host.holds(Server::class.java)) {
      throw RuntimeException("Host is not server")
    }

    // Dispose the host
    this.destroyHost()

    // notify the host type changed
    notifyHostTypeChanged(HostType.NONE)
  }

  fun registerService() {
    // if the host is not server then throw
    if (!host.holds(Server::class.java)) {
      throw RuntimeException("Host is not server")
    }

    // get the server
    val server = host.get() as Server

    // register the service
    server.registerService()
  }

  fun unregisterService() {
    // if the host is not server then throw
    if (!host.holds(Server::class.java)) {
      throw RuntimeException("Host is not server")
    }

    // get the server
    val server = host.get() as Server

    // unregister the service
    server.unregisterService()
  }

  fun isRegistered(): Boolean {
    // if the host is not server then throw
    if (!host.holds(Server::class.java)) {
      throw RuntimeException("Host is not server")
    }

    // get the server
    val server = host.get() as Server

    // return the server registered status
    return server.isRegistered()
  }

  fun reRegisterService() {
    // if the host is not server then throw
    if (!host.holds(Server::class.java)) {
      throw RuntimeException("Host is not server")
    }

    // get the server
    val server = host.get() as Server

    // re-register the service
    server.reRegisterService()
  }

  //---------------------- Client functions -----------------------//

  /**
   * Get the Server List object
   */
  fun getServerList(): Set<Device> {
    // if the host is not client then throw
    if (!host.holds(Client::class.java)) {
      throw RuntimeException("Host is not client")
    }

    // get the client
    val client = host.get() as Client

    // return the server list
    return client.getServerList()
  }

  /**
   * Connect to the server with the given host and port
   * number
   */
  fun connectToServer(server: Device) {
    // if the host is not client then throw
    if (!host.holds(Client::class.java)) {
      throw RuntimeException("Host is not client")
    }

    // get the client
    val client = host.get() as Client

    // connect to the server
    client.connectToServer(server)
  }

  /**
   * get the connected server address and port
   */
  fun getConnectedServer(): Device? {
    // if the host is not client then throw
    if (!host.holds(Client::class.java)) {
      throw RuntimeException("Host is not client")
    }

    // get the client
    val client = host.get() as Client

    // return the server address and port
    return client.getConnectedServer()
  }

  /**
   * Disconnect from the server
   */
  fun disconnectFromServer(server: Device) {
    // if the host is not client then throw
    if (!host.holds(Client::class.java)) {
      throw RuntimeException("Host is not client")
    }

    // get the client
    val client = host.get() as Client

    // connected server
    val s = client.getConnectedServer() ?: return

    // is not connected to the given server then return
    if ((s.ip != server.ip) || (s.port != server.port)) {
      return
    }

    // disconnect from the server
    client.disconnectFromServer()
  }

  /**
   * Dispose the Client
   */
  fun disposeClient() {
    // if the host is not client then throw
    if (!host.holds(Client::class.java)) {
      throw RuntimeException("Host is not client")
    }

    // Dispose the host
    this.destroyHost()

    // notify the host type changed
    notifyHostTypeChanged(HostType.NONE)
  }

  /**
   * @brief Get the UnAuthenticated Clients
   */
  fun getUnAuthenticatedClients(): List<Device> {
    // if the host is not server then throw
    if (!host.holds(Server::class.java)) {
      throw RuntimeException("Host is not server")
    }

    // get the server
    val server = host.get() as Server

    // return the unauthenticated clients
    return server.getUnauthenticatedClients()
  }

  fun startBrowsing() {
    // if the host is not client then throw
    if (!host.holds(Client::class.java)) {
      throw RuntimeException("Host is not client")
    }

    // get the client
    val client = host.get() as Client

    // start browsing
    client.startBrowsing()
  }

  fun stopBrowsing() {
    // if the host is not client then throw
    if (!host.holds(Client::class.java)) {
      throw RuntimeException("Host is not client")
    }

    // get the client
    val client = host.get() as Client

    // stop browsing
    client.stopBrowsing()
  }

  fun isBrowsing(): Boolean {
    // if the host is not client then throw
    if (!host.holds(Client::class.java)) {
      throw RuntimeException("Host is not client")
    }

    // get the client
    val client = host.get() as Client

    // return the browsing status
    return client.isBrowsing()
  }

  fun restartBrowsing() {
    // if the host is not client then throw
    if (!host.holds(Client::class.java)) {
      throw RuntimeException("Host is not client")
    }

    // get the client
    val client = host.get() as Client

    // restart browsing
    client.restartBrowsing()
  }

  //----------------------- Common functions -------------------------//

  /**
   * @brief Sync the clipboard data with the Group
   */
  fun syncClipboard(data: List<Pair<String, ByteArray>>) {
    // if the host is server then sync the clipboard
    if (host.holds(Server::class.java)) {
      (host.get() as Server).synchronize(data)
    }

    // if the host is not client then return
    if (host.holds(Client::class.java)) {
      (host.get() as Client).synchronize(data)
    }
  }

  //---------------------- Clipboard functions -----------------------//

  /**
   * @brief Get the Clipboard data
   */
  fun getClipboard(): List<Pair<String, ByteArray>> {
    return clipboard.getClipboardContent()
  }

  /**
   * @brief Set the Clipboard data
   */
  fun setClipboard(data: List<Pair<String, ByteArray>>) {
    clipboard.setClipboardContent(data)
  }

  //---------------------- General functions -----------------------//

  /**
   * IS the Host is Lastly Server
   */
  fun isLastlyHostIsServer(): Boolean {
    return storage.getHostIsLastlyServer()
  }

  /**
   * Delete ClipData by Index
   */
  fun deleteHistoryAt(index: Int) {
    val newHist = _history.value.toMutableList()
    newHist.removeAt(index)
    _history.value = newHist
  }

  /**
   * get the Host Type
   */
  fun getHostType(): HostType {
    return when {
      host.holds(Server::class.java) -> HostType.SERVER
      host.holds(Client::class.java) -> HostType.CLIENT
      else -> HostType.NONE
    }
  }

  override fun close() {
    this.scope.cancel()
  }
}
