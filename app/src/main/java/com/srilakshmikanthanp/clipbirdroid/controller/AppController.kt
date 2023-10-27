package com.srilakshmikanthanp.clipbirdroid.controller

import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.clipboard.Clipboard
import com.srilakshmikanthanp.clipbirdroid.network.syncing.Client
import com.srilakshmikanthanp.clipbirdroid.network.syncing.Server
import com.srilakshmikanthanp.clipbirdroid.types.aliases.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.types.device.Device

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

  private val serverGoneHandlers = mutableListOf<Client.OnServerGoneHandler>()

  fun addServerGoneHandler(handler: Client.OnServerGoneHandler) {
    serverGoneHandlers.add(handler)
  }

  fun removeServerGoneHandler(handler: Client.OnServerGoneHandler) {
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

  //----------------------- server Signals ------------------------//

  private val clientStateChangedHandlers = mutableListOf<Server.OnClientStateChangeHandler>()

  fun addClientStateChangedHandler(handler: Server.OnClientStateChangeHandler) {
    clientStateChangedHandlers.add(handler)
  }

  fun removeClientStateChangedHandler(handler: Server.OnClientStateChangeHandler) {
    clientStateChangedHandlers.remove(handler)
  }

  private val serverStateChangedHandlers = mutableListOf<Server.OnServerStateChangeHandler>()

  fun addServerStateChangedHandler(handler: Server.OnServerStateChangeHandler) {
    serverStateChangedHandlers.add(handler)
  }

  fun removeServerStateChangedHandler(handler: Server.OnServerStateChangeHandler) {
    serverStateChangedHandlers.remove(handler)
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

  //------------------------- Member Variables -------------------------//

  private val clipboard : Clipboard = Clipboard(context)
  private var server: Server? = null
  private var client: Client? = null

  //------------------------- private slots -------------------------//

  /**
   * Handle the Client State Changes (From server)
   */
  private fun handleClientStateChanged(client: Device, connected: Boolean) {
    TODO()
  }

  /**
   * Handle the Server Found (From client)
   */
  private fun handleServerFound(server: Device) {
    TODO()
  }

  /**
   * Handle the Server Status Changes (From client)
   */
  private fun handleServerStatusChanged(status: Boolean) {
    TODO()
  }

  //------------------------- public slots -------------------------//

  /**
   * Set Current Host As Server
   */
  fun setCurrentHostAsServer() {
    // TODO
  }

  /**
   * Set Current Host As Client
   */
  fun setCurrentHostAsClient() {
    // TODO
  }

  //------------------- Store functions ------------------------//

  /**
   * Clear Server Certificates
   */
  fun clearServerCertificates() {
    // TODO
  }

  /**
   * Clear Client Certificates
   */
  fun clearClientCertificates() {
    // TODO
  }

  //------------------- Server functions ------------------------//

  /**
   * Get the Clients that are connected to the server
   *
   * @return QList<QSslSocket*> List of clients
   */
  fun getConnectedClientsList(): List<Device> {
    TODO()
  }

  /**
   * Disconnect the client from the server and delete
   * the client
   * @param host ip address of the client
   * @param ip port number of the client
   */
  fun disconnectClient(client: Device) {
    TODO()
  }

  /**
   * Disconnect the all the clients from the server
   */
  fun disconnectAllClients() {
    TODO()
  }

  /**
   * Get the server QHostAddress and port
   */
  fun getServerInfo(): Device {
    TODO()
  }

  /**
   * The function that is called when the client is authenticated
   *
   * @param client the client that is currently processed
   */
  fun onClientAuthenticated(client: Device) {
    TODO()
  }

  /**
   * The function that is called when the client it not
   * authenticated
   *
   * @param client the client that is currently processed
   */
  fun onClientNotAuthenticated(client: Device) {
    TODO()
  }

  //---------------------- Client functions -----------------------//

  /**
   * Get the Server List object
   *
   * @return QList<types::device::Device> List of servers
   */
  fun getServerList(): List<Device> {
    TODO()
  }

  /**
   * Connect to the server with the given host and port
   * number
   *
   * @param host Host address
   * @param port Port number
   */
  fun connectToServer(host: Device) {
    TODO()
  }

  /**
   * Is Client Connected
   */
  fun isConnectedToServer(): Boolean {
    TODO()
  }

  /**
   * get the connected server address and port
   *
   * @return types::device::Device address and port
   */
  fun getConnectedServer(): Device {
    TODO()
  }

  /**
   * Disconnect from the server
   */
  fun disconnectFromServer(host: Device) {
    TODO()
  }

  //---------------------- General functions -----------------------//

  /**
   * IS the Host is Lastly Server
   */
  fun isLastlyHostIsServer(): Boolean {
    TODO()
  }
}
