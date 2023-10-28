package com.srilakshmikanthanp.clipbirdroid.controller

import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.clipboard.Clipboard
import com.srilakshmikanthanp.clipbirdroid.intface.OnAuthRequestHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnClientListChangeHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnClientStateChangeHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnConnectionErrorHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnServerFoundHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnServerGoneHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnServerListChangeHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnServerStateChangeHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnServerStatusChangeHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnSyncRequestHandler
import com.srilakshmikanthanp.clipbirdroid.network.syncing.Client
import com.srilakshmikanthanp.clipbirdroid.network.syncing.Server
import com.srilakshmikanthanp.clipbirdroid.store.Storage
import com.srilakshmikanthanp.clipbirdroid.types.aliases.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.types.device.Device
import com.srilakshmikanthanp.clipbirdroid.types.variant.Variant

class AppController(private val sslConfig: SSLConfig, private val context: Context) {
  //----------------------- client Signals -------------------------//
  private val serverListChangedHandlers = mutableListOf<OnServerListChangeHandler>()

  fun addServerListChangedHandler(handler: OnServerListChangeHandler) {
    serverListChangedHandlers.add(handler)
  }

  fun removeServerListChangedHandler(handler: OnServerListChangeHandler) {
    serverListChangedHandlers.remove(handler)
  }

  private val serverFoundHandlers = mutableListOf<OnServerFoundHandler>()

  fun addServerFoundHandler(handler: OnServerFoundHandler) {
    serverFoundHandlers.add(handler)
  }

  fun removeServerFoundHandler(handler: OnServerFoundHandler) {
    serverFoundHandlers.remove(handler)
  }

  private val serverGoneHandlers = mutableListOf<OnServerGoneHandler>()

  fun addServerGoneHandler(handler: OnServerGoneHandler) {
    serverGoneHandlers.add(handler)
  }

  fun removeServerGoneHandler(handler: OnServerGoneHandler) {
    serverGoneHandlers.remove(handler)
  }

  private val connectionErrorHandlers = mutableListOf<OnConnectionErrorHandler>()

  fun addConnectionErrorHandler(handler: OnConnectionErrorHandler) {
    connectionErrorHandlers.add(handler)
  }

  fun removeConnectionErrorHandler(handler: OnConnectionErrorHandler) {
    connectionErrorHandlers.remove(handler)
  }

  private val serverStatusChangedHandlers = mutableListOf<OnServerStatusChangeHandler>()

  fun addServerStatusChangedHandler(handler: OnServerStatusChangeHandler) {
    serverStatusChangedHandlers.add(handler)
  }

  fun removeServerStatusChangedHandler(handler: OnServerStatusChangeHandler) {
    serverStatusChangedHandlers.remove(handler)
  }

  //----------------------- server Signals ------------------------//

  private val clientStateChangedHandlers = mutableListOf<OnClientStateChangeHandler>()

  fun addClientStateChangedHandler(handler: OnClientStateChangeHandler) {
    clientStateChangedHandlers.add(handler)
  }

  fun removeClientStateChangedHandler(handler: OnClientStateChangeHandler) {
    clientStateChangedHandlers.remove(handler)
  }

  private val serverStateChangedHandlers = mutableListOf<OnServerStateChangeHandler>()

  fun addServerStateChangedHandler(handler: OnServerStateChangeHandler) {
    serverStateChangedHandlers.add(handler)
  }

  fun removeServerStateChangedHandler(handler: OnServerStateChangeHandler) {
    serverStateChangedHandlers.remove(handler)
  }

  private val authRequestHandlers = mutableListOf<OnAuthRequestHandler>()

  fun addAuthRequestHandler(handler: OnAuthRequestHandler) {
    authRequestHandlers.add(handler)
  }

  fun removeAuthRequestHandler(handler: OnAuthRequestHandler) {
    authRequestHandlers.remove(handler)
  }

  private val clientListChangedHandlers = mutableListOf<OnClientListChangeHandler>()

  fun addClientListChangedHandler(handler: OnClientListChangeHandler) {
    clientListChangedHandlers.add(handler)
  }

  fun removeClientListChangedHandler(handler: OnClientListChangeHandler) {
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

  //------------------------- Member Variables -------------------------//

  private val storage: Storage = Storage.getInstance(context)
  private val host: Variant = Variant()
  private val clipboard : Clipboard = Clipboard(context)

  //----------------------- private notifiers ------------------------//

  /**
   * Notify the server list changed (Client)
   */
  private fun notifyServerListChanged(servers: List<Device>) {
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
  private fun notifyServerStatusChanged(status: Boolean) {
    for (handler in serverStatusChangedHandlers) {
      handler.onServerStatusChanged(status)
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
    for (handler in serverStateChangedHandlers) {
      handler.onServerStateChanged(status)
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

  //------------------------- private slots -------------------------//

  /**
   * Handle the Data going to destroy on Variant
   */
  private fun handleVariantDataDestroy(obj: Any) {
    // if the data is Server Instance
    if (obj is Server) {
      // disconnect the signals from Server
      obj.removeServerStateChangeHandler(::notifyServerStateChanged)
      obj.removeClientStateChangeHandler(::handleClientStateChanged)
      obj.removeClientStateChangeHandler(::notifyClientStateChanged)
      obj.removeAuthRequestHandler(::notifyAuthRequest)
      obj.removeSyncRequestHandler(::notifySyncRequest)
      obj.removeClientListChangeHandler(::notifyClientListChanged)

      // Disconnect the signals to Server
      clipboard.removeClipboardChangeListener(obj::syncItems)

      // stop the server
      obj.stopServer()
    }

    // if the data is Client Instance
    if (obj is Client) {
      // disconnect the signals from Client
      obj.removeServerListChangeHandler(::notifyServerListChanged)
      obj.removeServerFoundHandler(::handleServerFound)
      obj.removeServerFoundHandler(::notifyServerFound)
      obj.removeServerGoneHandler(::notifyServerGone)
      obj.removeServerStatusChangeHandler(::handleServerStatusChanged)
      obj.removeServerStatusChangeHandler(::notifyServerStatusChanged)
      obj.removeSyncRequestHandler(::notifySyncRequest)
      obj.removeConnectionErrorHandler(::notifyConnectionError)

      // Disconnect the signals to Client
      clipboard.removeClipboardChangeListener(obj::syncItems)

      // stop the client
      obj.stopBrowsing()

      // if connected to server then disconnect
      if (obj.isConnected()) obj.disconnectFromServer()
    }
  }

  /**
   * Handle the Client State Changes (From server)
   */
  private fun handleClientStateChanged(client: Device, connected: Boolean) {
    // if the host is not server then throw error
    if (this.host.holds(Server::class.java)) {
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
    if (client.isConnected()) return

    // if the server is not found then return
    if (storage.hasServerCert(server.name)) {
      client.connectToServerSecured(server)
    }
  }

  /**
   * Handle the Server Status Changes (From client)
   */
  private fun handleServerStatusChanged(status: Boolean) {
    // if the host is not client then throw error
    if (!host.holds(Client::class.java)) {
      throw RuntimeException("Host is not client")
    }

    // get the client and disconnect the signals
    val client = host.get() as Client

    // if the client is connected then connect the signals
    if (!status) {
      clipboard.removeClipboardChangeListener(client::syncItems)
    } else {
      clipboard.addClipboardChangeListener(client::syncItems)
      val cert = client.getConnectedServerCertificate()
      val name = client.getConnectedServer().name
      storage.setServerCert(name, cert)
    }
  }

  //------------------------- Initializer -------------------------//

  init { host.addOnDataDestroyHandler(::handleVariantDataDestroy) }

  //------------------------- public slots -------------------------//

  /**
   * Set Current Host As Server
   */
  fun setCurrentHostAsServer() {
    // create the server object with context
    val server: Server = host.set(Server(context)) as Server

    // set the ssl configuration
    server.setSslConfig(sslConfig)

    // connect the server state changed signal
    server.addServerStateChangeHandler(::notifyServerStateChanged)

    // connect the client state changed signal
    server.addClientStateChangeHandler(::handleClientStateChanged)
    server.addClientStateChangeHandler(::notifyClientStateChanged)

    // connect the auth request signal
    server.addAuthRequestHandler(::notifyAuthRequest)

    // connect the sync request signal
    server.addSyncRequestHandler(clipboard::setClipboardContent)
    server.addSyncRequestHandler(::notifySyncRequest)

    // connect the OnClipboardChange signal to the server
    clipboard.addClipboardChangeListener(server::syncItems)

    // connect the client list changed signal
    server.addClientListChangeHandler(::notifyClientListChanged)

    // set the host is server
    storage.setHostIsLastlyServer(true)

    // Start the server to listen and accept the client
    server.startServer()
  }

  /**
   * Set Current Host As Client
   */
  fun setCurrentHostAsClient() {
    // Create the Client object with context
    val client: Client = host.set(Client(context)) as Client

    // Set the SSL Configuration
    client.setSslConfig(sslConfig)

    // Connect the server list changed signal
    client.addServerListChangeHandler(::notifyServerListChanged)

    // Connect the server found signal
    client.addServerFoundHandler(::handleServerFound)
    client.addServerFoundHandler(::notifyServerFound)

    // Connect the server gone signal
    client.addServerGoneHandler(::notifyServerGone)

    // Connect the server status changed signal
    client.addServerStatusChangeHandler(::handleServerStatusChanged)
    client.addServerStatusChangeHandler(::notifyServerStatusChanged)

    // Connect the sync request signal
    client.addSyncRequestHandler(clipboard::setClipboardContent)
    client.addSyncRequestHandler(::notifySyncRequest)

    // Connect the connection error signal
    client.addConnectionErrorHandler(::notifyConnectionError)

    // Connect the clipboard change signal
    clipboard.addClipboardChangeListener(client::syncItems)

    // set the host is client
    storage.setHostIsLastlyServer(false)

    // Start the discovery
    client.startBrowsing()
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
    if (!host.holds(Server::class.java)) {
      throw RuntimeException("Host is not server")
    }

    return (host.get() as Server).getClients()
  }

  /**
   * Disconnect the client from the server and delete
   * the client
   */
  fun disconnectClient(client: Device) {
    // if the host is not server then throw
    if (!host.holds(Server::class.java)) {
      throw RuntimeException("Host is not server")
    }

    // find the client with the given host and ip
    val match = { i: Device -> i.name == client.name }

    // get the list of clients
    val clients = getConnectedClientsList()

    // find the client
    val it = clients.find(match) ?: throw RuntimeException("Client not found")

    // disconnect the client
    (host.get() as Server).disconnectClient(client)
  }

  /**
   * Disconnect the all the clients from the server
   */
  fun disconnectAllClients() {
    // if the host is not server then throw
    if (!host.holds(Server::class.java)) {
      throw RuntimeException("Host is not server")
    }

    // disconnect all the clients
    (host.get() as Server).disconnectAllClients()
  }

  /**
   * Get the server QHostAddress and port
   */
  fun getServerInfo(): Device {
    // if the host is not server then throw
    if (!host.holds(Server::class.java)) {
      throw RuntimeException("Host is not server")
    }

    // return the server address and port
    return (host.get() as Server).getServerInfo()
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

  //---------------------- Client functions -----------------------//

  /**
   * Get the Server List object
   */
  fun getServerList(): List<Device> {
    // if the host is not client then throw
    if (!host.holds(Client::class.java)) {
      throw RuntimeException("Host is not client")
    }

    // return the server list
    return (host.get() as Client).getServerList()
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
   * Is Client Connected
   */
  fun isConnectedToServer(): Boolean {
    // if the host is not client then throw
    if (!host.holds(Client::class.java)) {
      throw RuntimeException("Host is not client")
    }

    // return the connection status
    return (host.get() as Client).isConnected()
  }

  /**
   * get the connected server address and port
   */
  fun getConnectedServer(): Device {
    // if the host is not client then throw
    if (!host.holds(Client::class.java)) {
      throw RuntimeException("Host is not client")
    }

    // return the server address and port
    return (host.get() as Client).getConnectedServer()
  }

  /**
   * Disconnect from the server
   */
  fun disconnectFromServer(server: Device) {
    // if the host is not client then throw
    if (!host.holds(Client::class.java)) {
      throw RuntimeException("Host is not client")
    }

    // disconnect from the server
    (host.get() as Client).disconnectFromServer()
  }

  //----------------------- Common functions -------------------------//

  /**
   * @brief Sync the clipboard data with the Group
   */
  fun syncClipboard(data: List<Pair<String, ByteArray>>) {
    if (host.holds(Server::class.java)) {
      (host.get() as Server).syncItems(data)
    }

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
}
