package com.srilakshmikanthanp.clipbirdroid.controller

import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.clipboard.Clipboard
import com.srilakshmikanthanp.clipbirdroid.common.enums.HostType
import com.srilakshmikanthanp.clipbirdroid.common.types.Device
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.common.variant.Variant
import com.srilakshmikanthanp.clipbirdroid.constant.appMaxHistory
import com.srilakshmikanthanp.clipbirdroid.store.Storage
import com.srilakshmikanthanp.clipbirdroid.syncing.client.Client
import com.srilakshmikanthanp.clipbirdroid.syncing.client.Client.OnBrowsingStartFailedHandler
import com.srilakshmikanthanp.clipbirdroid.syncing.client.Client.OnBrowsingStopFailedHandler
import com.srilakshmikanthanp.clipbirdroid.syncing.common.OnSyncRequestHandler
import com.srilakshmikanthanp.clipbirdroid.syncing.server.Server
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppController(private val sslConfig: SSLConfig, private val context: Context) {
  //----------------------- client Signals -------------------------//
  private val serverListChangedHandlers = mutableListOf<Client.OnServerListChangeHandler>()

  fun addServerListChangedHandler(handler: Client.OnServerListChangeHandler) {
    serverListChangedHandlers.add(handler)
  }

  fun removeServerListChangedHandler(handler: Client.OnServerListChangeHandler) {
    serverListChangedHandlers.remove(handler)
  }

  private val serverFoundHandlers = mutableListOf<Client.OnServerFoundHandler>()

  fun addServerFoundHandler(handler: Client.OnServerFoundHandler) {
    serverFoundHandlers.add(handler)
  }

  fun removeServerFoundHandler(handler: Client.OnServerFoundHandler) {
    serverFoundHandlers.remove(handler)
  }

  private val serverGoneHandlers = mutableListOf<OnServerGoneHandler>()

  fun interface OnServerGoneHandler {
    fun onServerGone(server: Device)
  }

  fun addServerGoneHandler(handler: OnServerGoneHandler) {
    serverGoneHandlers.add(handler)
  }

  fun removeServerGoneHandler(handler: OnServerGoneHandler) {
    serverGoneHandlers.remove(handler)
  }

  private val connectionErrorHandlers = mutableListOf<Client.OnConnectionErrorHandler>()

  fun addConnectionErrorHandler(handler: Client.OnConnectionErrorHandler) {
    connectionErrorHandlers.add(handler)
  }

  fun removeConnectionErrorHandler(handler: Client.OnConnectionErrorHandler) {
    connectionErrorHandlers.remove(handler)
  }

  private val serverStatusChangedHandlers = mutableListOf<Client.OnServerStatusChangeHandler>()

  fun addServerStatusChangedHandler(handler: Client.OnServerStatusChangeHandler) {
    serverStatusChangedHandlers.add(handler)
  }

  fun removeServerStatusChangedHandler(handler: Client.OnServerStatusChangeHandler) {
    serverStatusChangedHandlers.remove(handler)
  }

  private val browsingStatusChangeHandlers = mutableListOf<Client.OnBrowsingStatusChangeHandler>()

  fun addBrowsingStatusChangeHandler(handler: Client.OnBrowsingStatusChangeHandler) {
    browsingStatusChangeHandlers.add(handler)
  }

  fun removeBrowsingStatusChangeHandler(handler: Client.OnBrowsingStatusChangeHandler) {
    browsingStatusChangeHandlers.remove(handler)
  }

  private val onStartBrowsingFailedHandlers = mutableListOf<Client.OnBrowsingStartFailedHandler>()

  fun addBrowsingStartFailedHandler(handler: OnBrowsingStartFailedHandler) {
    onStartBrowsingFailedHandlers.add(handler)
  }

  fun removeBrowsingStartFailedHandler(handler: OnBrowsingStartFailedHandler) {
    onStartBrowsingFailedHandlers.remove(handler)
  }

  private val onStopBrowsingFailedHandlers = mutableListOf<OnBrowsingStopFailedHandler>()

  fun addBrowsingStopFailedHandler(handler: OnBrowsingStopFailedHandler) {
    onStopBrowsingFailedHandlers.add(handler)
  }

  fun removeBrowsingStopFailedHandler(handler: OnBrowsingStopFailedHandler) {
    onStopBrowsingFailedHandlers.remove(handler)
  }

  //----------------------- server Signals ------------------------//

  private val clientStateChangedHandlers = mutableListOf<Server.OnClientStateChangeHandler>()

  fun addClientStateChangedHandler(handler: Server.OnClientStateChangeHandler) {
    clientStateChangedHandlers.add(handler)
  }

  fun removeClientStateChangedHandler(handler: Server.OnClientStateChangeHandler) {
    clientStateChangedHandlers.remove(handler)
  }

  private val mdnsRegisterStatusChangeHandlers = mutableListOf<Server.OnMdnsRegisterStatusChangeHandler>()

  fun addServerStateChangedHandler(handler: Server.OnMdnsRegisterStatusChangeHandler) {
    mdnsRegisterStatusChangeHandlers.add(handler)
  }

  fun removeServerStateChangedHandler(handler: Server.OnMdnsRegisterStatusChangeHandler) {
    mdnsRegisterStatusChangeHandlers.remove(handler)
  }

  private val mdnsServiceRegisterFailedHandlers = mutableListOf<Server.OnMdnsServiceRegisterFailedHandler>()

  fun addMdnsServiceRegisterFailedHandler(handler: Server.OnMdnsServiceRegisterFailedHandler) {
    mdnsServiceRegisterFailedHandlers.add(handler)
  }

  fun removeMdnsServiceRegisterFailedHandler(handler: Server.OnMdnsServiceRegisterFailedHandler) {
    mdnsServiceRegisterFailedHandlers.remove(handler)
  }

  private val mdnsServiceUnregisterFailedHandlers = mutableListOf<Server.OnMdnsServiceUnregisterFailedHandler>()

  fun addMdnsServiceUnregisterFailedHandler(handler: Server.OnMdnsServiceUnregisterFailedHandler) {
    mdnsServiceUnregisterFailedHandlers.add(handler)
  }

  fun removeMdnsServiceUnregisterFailedHandler(handler: Server.OnMdnsServiceUnregisterFailedHandler) {
    mdnsServiceUnregisterFailedHandlers.remove(handler)
  }

  private val authRequestHandlers = mutableListOf<Server.OnAuthRequestHandler>()

  fun addAuthRequestHandler(handler: Server.OnAuthRequestHandler) {
    authRequestHandlers.add(handler)
  }

  fun removeAuthRequestHandler(handler: Server.OnAuthRequestHandler) {
    authRequestHandlers.remove(handler)
  }

  private val clientListChangedHandlers = mutableListOf<Server.OnClientListChangeHandler>()

  fun addClientListChangedHandler(handler: Server.OnClientListChangeHandler) {
    clientListChangedHandlers.add(handler)
  }

  fun removeClientListChangedHandler(handler: Server.OnClientListChangeHandler) {
    clientListChangedHandlers.remove(handler)
  }

  //----------------------- Common Signals ------------------------//

  private val syncRequestHandlers = mutableListOf<OnSyncRequestHandler>()

  fun addSyncRequestHandler(handler: OnSyncRequestHandler) {
    syncRequestHandlers.add(handler)
  }

  fun removeSyncRequestHandler(handler: OnSyncRequestHandler) {
    syncRequestHandlers.remove(handler)
  }

  private val hostTypeChangeHandlers = mutableListOf<OnHostTypeChangeHandler>()

  fun interface OnHostTypeChangeHandler {
    fun onHostTypeChanged(host: HostType)
  }

  fun addHostTypeChangeHandler(handler: OnHostTypeChangeHandler) {
    hostTypeChangeHandlers.add(handler)
  }

  fun removeHostTypeChangeHandler(handler: OnHostTypeChangeHandler) {
    hostTypeChangeHandlers.remove(handler)
  }

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
      clipboard.removeClipboardChangeListener(server::syncItems)
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
      clipboard.removeClipboardChangeListener(client::syncItems)
    }
  }

  //------------------------- Member Variables -------------------------//

  // Private
  private val _history = MutableStateFlow(emptyList<List<Pair<String, ByteArray>>>().toMutableList())
  private val storage: Storage = Storage.getInstance(context)
  private val host: Variant = Variant()
  private val clipboard: Clipboard = Clipboard(context)
  private val TAG = "AppController"

  // Public
  val history = _history.asStateFlow()

  //----------------------- private notifiers ------------------------//

  /**
   * Notify the server list changed (Client)
   */
  private fun notifyServerListChanged(servers: Set<Device>) {
    for (handler in serverListChangedHandlers) {
      handler.onServerListChanged(servers)
    }
  }

  /**
   * Notify the server Found (Client)
   */
  private fun notifyServerFound(server: Device) {
    for (handler in serverFoundHandlers) {
      handler.onServerFound(server)
    }
  }

  /**
   * Notify the server gone (Client)
   */
  private fun notifyServerGone(server: Device) {
    for (handler in serverGoneHandlers) {
      handler.onServerGone(server)
    }
  }

  /**
   * Notify the connection error (Client)
   */
  private fun notifyConnectionError(error: String) {
    for (handler in connectionErrorHandlers) {
      handler.onConnectionError(error)
    }
  }

  /**
   * Notify the server status changed (Client)
   */
  private fun notifyServerStatusChanged(status: Boolean, server: Device) {
    for (handler in serverStatusChangedHandlers) {
      handler.onServerStatusChanged(status, server)
    }
  }

  /**
   * Notify the client state changed (Server)
   */
  private fun notifyClientStateChanged(client: Device, connected: Boolean) {
    for (handler in clientStateChangedHandlers) {
      handler.onClientStateChanged(client, connected)
    }
  }

  /**
   * Notify the server state changed (Server)
   */
  private fun notifyServerStateChanged(status: Boolean) {
    for (handler in mdnsRegisterStatusChangeHandlers) {
      handler.onMdnsRegisterStatusChanged(status)
    }
  }

  /**
   * Notify the auth request (Server)
   */
  private fun notifyAuthRequest(client: Device) {
    for (handler in authRequestHandlers) {
      handler.onAuthRequest(client)
    }
  }

  /**
   * Notify the client list changed (Server)
   */
  private fun notifyClientListChanged(clients: List<Device>) {
    for (handler in clientListChangedHandlers) {
      handler.onClientListChanged(clients)
    }
  }

  /**
   * Notify the sync request (Common)
   */
  private fun notifySyncRequest(data: List<Pair<String, ByteArray>>) {
    for (handler in syncRequestHandlers) {
      handler.onSyncRequest(data)
    }
  }

  /**
   * Notify the host type changed (Common)
   */
  private fun notifyHostTypeChanged(host: HostType) {
    for (handler in hostTypeChangeHandlers) {
      handler.onHostTypeChanged(host)
    }
  }

  private fun notifyBrowsingStatusChanged(isBrowsing: Boolean) {
    for (handler in browsingStatusChangeHandlers) {
      handler.onBrowsingStatusChanged(isBrowsing)
    }
  }

  private fun notifyBrowsingStartFailed(error: Int) {
    for (handler in onStartBrowsingFailedHandlers) {
      handler.onStartBrowsingFailed(error)
    }
  }

  private fun notifyBrowsingStopFailed(error: Int) {
    for (handler in onStopBrowsingFailedHandlers) {
      handler.onStopBrowsingFailed(error)
    }
  }

  private fun notifyMdnsServiceRegisterFailed(error: Int) {
    for (handler in mdnsServiceRegisterFailedHandlers) {
      handler.onServiceRegistrationFailed(error)
    }
  }

  private fun notifyMdnsServiceUnregisterFailed(error: Int) {
    for (handler in mdnsServiceUnregisterFailedHandlers) {
      handler.onServiceUnregistrationFailed(error)
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
      clipboard.addClipboardChangeListener(client::syncItems)
      val cert = client.getConnectedServerCertificate()
      val name = srv.name
      storage.setServerCert(name, cert)
      return
    }

    // Remove the clipboard change listener
    clipboard.removeClipboardChangeListener(client::syncItems)

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
    clipboard.addClipboardChangeListener(server::syncItems)

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
    clipboard.addClipboardChangeListener(client::syncItems)

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
      (host.get() as Server).syncItems(data)
    }

    // if the host is not client then return
    if (host.holds(Client::class.java)) {
      (host.get() as Client).syncItems(data)
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
}
