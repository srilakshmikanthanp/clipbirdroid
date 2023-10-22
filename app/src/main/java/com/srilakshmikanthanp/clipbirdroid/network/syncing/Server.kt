package com.srilakshmikanthanp.clipbirdroid.network.syncing

import com.srilakshmikanthanp.clipbirdroid.types.device.Device
import io.netty.handler.ssl.SslContext

/**
 * Client State Change Handler
 */
interface OnClientStateChangeHandler {
  fun onClientStateChanged(device: Device, connected: Boolean)
}

/**
 * Server State Change Handler
 */
interface OnServerStateChangeHandler {
  fun onServerStateChanged(started: Boolean)
}

/**
 * Auth Request Handler
 */
interface OnAuthRequestHandler {
  fun onAuthRequest(client: Device)
}

/**
 * Sync Request Handler
 */
interface OnSyncRequestHandler {
  fun onSyncRequest(items: List<Pair<String, ByteArray>>)
}

/**
 * Client List Change Handler
 */
interface OnClientListChangeHandler {
  fun onClientListChanged(clients: List<Device>)
}

/**
 * A SSl Server Using Netty as a Backend
 */
class Server {
  // Client State Change Handlers
  private val clientStateChangeHandlers = mutableListOf<OnClientStateChangeHandler>()

  // Server State Change Handlers
  private val serverStateChangeHandlers = mutableListOf<OnServerStateChangeHandler>()

  // Auth Request Handlers
  private val authRequestHandlers = mutableListOf<OnAuthRequestHandler>()

  // Sync Request Handlers
  private val syncRequestHandlers = mutableListOf<OnSyncRequestHandler>()

  // Client List Change Handlers
  private val clientListChangeHandlers = mutableListOf<OnClientListChangeHandler>()

  // Ssl Context
  var sslContext: SslContext? = null

  // Netty's SSL server Instance


  /**
   * Add Client State Change Handler
   */
  fun addClientStateChangeHandler(handler: OnClientStateChangeHandler) {
    clientStateChangeHandlers.add(handler)
  }

  /**
   * Remove Client State Change Handler
   */
  fun removeClientStateChangeHandler(handler: OnClientStateChangeHandler) {
    clientStateChangeHandlers.remove(handler)
  }

  /**
   * Add Server State Change Handler
   */
  fun addServerStateChangeHandler(handler: OnServerStateChangeHandler) {
    serverStateChangeHandlers.add(handler)
  }

  /**
   * Remove Server State Change Handler
   */
  fun removeServerStateChangeHandler(handler: OnServerStateChangeHandler) {
    serverStateChangeHandlers.remove(handler)
  }

  /**
   * Add Auth Request Handler
   */
  fun addAuthRequestHandler(handler: OnAuthRequestHandler) {
    authRequestHandlers.add(handler)
  }

  /**
   * Remove Auth Request Handler
   */
  fun removeAuthRequestHandler(handler: OnAuthRequestHandler) {
    authRequestHandlers.remove(handler)
  }

  /**
   * Add Sync Request Handler
   */
  fun addSyncRequestHandler(handler: OnSyncRequestHandler) {
    syncRequestHandlers.add(handler)
  }

  /**
   * Remove Sync Request Handler
   */
  fun removeSyncRequestHandler(handler: OnSyncRequestHandler) {
    syncRequestHandlers.remove(handler)
  }

  /**
   * Add Client List Change Handler
   */
  fun addClientListChangeHandler(handler: OnClientListChangeHandler) {
    clientListChangeHandlers.add(handler)
  }

  /**
   * Remove Client List Change Handler
   */
  fun removeClientListChangeHandler(handler: OnClientListChangeHandler) {
    clientListChangeHandlers.remove(handler)
  }
}
