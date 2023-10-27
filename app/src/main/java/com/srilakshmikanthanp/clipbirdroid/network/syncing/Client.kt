package com.srilakshmikanthanp.clipbirdroid.network.syncing

import android.content.Context
import android.util.Log
import com.srilakshmikanthanp.clipbirdroid.network.packets.Authentication
import com.srilakshmikanthanp.clipbirdroid.network.packets.InvalidPacket
import com.srilakshmikanthanp.clipbirdroid.network.packets.SyncingItem
import com.srilakshmikanthanp.clipbirdroid.network.packets.SyncingPacket
import com.srilakshmikanthanp.clipbirdroid.network.service.mdns.Browser
import com.srilakshmikanthanp.clipbirdroid.types.device.Device
import com.srilakshmikanthanp.clipbirdroid.types.enums.AuthStatus
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandler
import io.netty.handler.ssl.SslHandler
import java.net.InetSocketAddress
import java.security.PrivateKey
import java.security.cert.X509Certificate

/**
 * Client Class for Syncing the Clipboard
 */
class Client(context: Context): Browser.BrowserListener, ChannelInboundHandler {
  // List handlers for server list changed
  private val onServerListChangeHandlers = mutableListOf<OnServerListChangeHandler>()

  // List handlers for server found
  private val onServerFoundHandlers = mutableListOf<OnServerFoundHandler>()

  // List handlers for server gone
  private val onServerGoneHandlers = mutableListOf<OnServerGoneHandler>()

  // List handlers for server status changed
  private val onServerStatusChangeHandlers = mutableListOf<OnServerStatusChangeHandler>()

  // List handlers for connection error
  private val onConnectionErrorHandlers = mutableListOf<OnConnectionErrorHandler>()

  // List handlers for sync request
  private val onSyncRequestHandlers = mutableListOf<OnSyncRequestHandler>()

  // TAG for logging
  companion object {
    val TAG = "Client"
  }

  // Interface for On Server List Changed
  interface OnServerListChangeHandler {
    fun OnServerListChanged(servers: List<Device>)
  }

  // Interface for On Server Found
  interface OnServerFoundHandler {
    fun OnServerFound(server: Device)
  }

  // Interface for On Server Gone
  interface OnServerGoneHandler {
    fun OnServerGone(server: Device)
  }

  // Interface for On Server state changed
  interface OnServerStatusChangeHandler {
    fun OnServerStatusChanged(isConnected: Boolean)
  }

  // Interface for On Connection Error
  interface OnConnectionErrorHandler {
    fun OnConnectionError(error: String)
  }

  // Interface for On Sync Request
  interface OnSyncRequestHandler {
    fun OnSyncRequest(items: List<Pair<String, ByteArray>>)
  }

  // Ssl configuration
  private var sslConfig: Pair<PrivateKey, X509Certificate>? = null;

  // List of servers
  private val servers = mutableListOf<Device>()

  // Channel for communication
  private var channel: Channel? = null;

  // Browser
  private val browser = Browser(context)

  /**
   * Notify all the listeners for server list changed
   */
  private fun notifyServerListChanged() {
    for (handler in onServerListChangeHandlers) {
      handler.OnServerListChanged(servers)
    }
  }

  /**
   * Notify all the listeners for server found
   */
  private fun notifyServerFound(server: Device) {
    for (handler in onServerFoundHandlers) {
      handler.OnServerFound(server)
    }
  }

  /**
   * Notify all the listeners for server gone
   */
  private fun notifyServerGone(server: Device) {
    for (handler in onServerGoneHandlers) {
      handler.OnServerGone(server)
    }
  }

  /**
   * Notify all the listeners for server status changed
   */
  private fun notifyServerStatusChanged(isConnected: Boolean) {
    for (handler in onServerStatusChangeHandlers) {
      handler.OnServerStatusChanged(isConnected)
    }
  }

  /**
   * Notify all the listeners for connection error
   */
  private fun notifyConnectionError(error: String) {
    for (handler in onConnectionErrorHandlers) {
      handler.OnConnectionError(error)
    }
  }

  /**
   * Notify all the listeners for sync request
   */
  private fun notifySyncRequest(items: List<Pair<String, ByteArray>>) {
    for (handler in onSyncRequestHandlers) {
      handler.OnSyncRequest(items)
    }
  }

  /**
   * Process the Authentication Packet
   */
  private fun onAuthentication(ctx: ChannelHandlerContext, m: Authentication) {
    if (m.getAuthStatus() == AuthStatus.AuthOkay) {
      notifyServerStatusChanged(true)
    }
  }

  /**
   * Process the InvalidPacket
   */
  private fun onInvalidPacket(ctx: ChannelHandlerContext, m: InvalidPacket) {
    Log.e(TAG, "Invalid Packet ${m.getErrorCode()}: ${m.getErrorMessage().toString()}")
  }

  /**
   * Process the SyncingPacket
   */
  private fun onSyncingPacket(ctx: ChannelHandlerContext, m: SyncingPacket) {
    // list of items to be synced
    val items = mutableListOf<Pair<String, ByteArray>>()

    // add the items to the list
    for (item in m.getItems()) {
      items.add(Pair(String(item.getMimeType()), item.getPayload()))
    }

    // notify the sync handlers
    notifySyncRequest(items)
  }

  /**
   * Sent Packet to Server
   */
  private fun<T> sendPacket(packet: T) {
    // check if channel is initialized
    if (!this.isConnected()) {
      throw Exception("Channel is not initialized")
    }

    // send packet
    channel?.writeAndFlush(packet)
  }

  /**
   * Init
   */
  init {
    this.browser.addListener(this)
  }

  /**
   * Is connected to server
   */
  fun isConnected(): Boolean {
    return channel != null && channel!!.isActive
  }

  /**
   * Set the ssl configuration
   */
  fun setSslConfig(sslConfig: Pair<PrivateKey, X509Certificate>) {
    this.sslConfig = sslConfig
  }

  /**
   * Get the ssl configuration
   */
  fun getSslConfig(): Pair<PrivateKey, X509Certificate>? {
    return this.sslConfig
  }

  /**
   * Sync the Items
   */
  fun syncItems(items: List<Pair<String, ByteArray>>) {
    // check if channel is initialized
    if (!this.isConnected()) throw Exception("Channel is not initialized")

    // create the syncing Items
    val syncingItems = items.map {
      return@map SyncingItem(it.first.toByteArray(), it.second)
    }

    // Array
    val array = syncingItems.toTypedArray()

    // send the packet
    this.sendPacket(SyncingPacket(array))
  }

  /**
   * Get the Server List
   */
  fun getServerList(): List<Device> {
    return servers
  }

  /**
   * Connect to server Secured
   */
  fun connectToServerSecured(server: Device) {
    // TODO
  }

  /**
   * Connect to server
   */
  fun connectToServer(server: Device) {
    // TODO
  }

  /**
   * Get the Connected Server
   */
  fun getConnectedServer(): Device? {
    if (!this.isConnected()) throw RuntimeException("Not Connected to server")

    val addr = channel!!.remoteAddress() as InetSocketAddress
    val ssl = channel!!.pipeline().get("ssl") as SslHandler
    val cert = ssl.engine().session.peerCertificates[0] as X509Certificate
    val name = cert.subjectDN.name

    return Device(addr.address, addr.port, name)
  }

  /**
   * Disconnect from the server
   */
  fun disconnectFromServer() {
    if (!this.isConnected()) {
      throw RuntimeException("Not Connected to server")
    } else {
      channel!!.close()
    }
  }

  /**
   * Get the Connected server Certificate
   */
  fun getConnectedServerCertificate(): X509Certificate {
    if (!this.isConnected()) throw RuntimeException("Not Connected to server")
    val ssl = channel!!.pipeline().get("ssl") as SslHandler
    return ssl.engine().session.peerCertificates[0] as X509Certificate
  }

  /**
   * Called when a service is lost.
   */
  override fun onServiceRemoved(device: Device) {
    notifyServerFound(device)
    this.servers.add(device)
    notifyServerListChanged()
  }

  /**
   * Called when a service is found.
   */
  override fun onServiceAdded(device: Device) {
    if (!this.servers.contains(device)) {
      return
    }

    notifyServerGone(device)
    this.servers.remove(device)
    notifyServerListChanged()
  }

  /**
   * Gets called after the ChannelHandler was added to the actual context and it's
   * ready to handle events.
   */
  override fun handlerAdded(ctx: ChannelHandlerContext?) {
    // Do Nothing
  }

  /**
   * Gets called after the ChannelHandler was removed from the actual context and
   * it doesn't handle events anymore.
   */
  override fun handlerRemoved(ctx: ChannelHandlerContext?) {
    // Do Nothing
  }

  /**
   * Gets called if a Throwable was thrown.
   */
  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable?) {
    Log.e(TAG, cause?.message, cause); ctx.close()
  }

  /**
   * The Channel of the ChannelHandlerContext was registered with its EventLoop
   */
  override fun channelRegistered(ctx: ChannelHandlerContext?) {
    // Do Nothing
  }

  /**
   * The Channel of the ChannelHandlerContext was unregistered from its EventLoop
   */
  override fun channelUnregistered(ctx: ChannelHandlerContext?) {
    // Do Nothing
  }

  /**
   * The Channel of the ChannelHandlerContext is now active
   */
  override fun channelActive(ctx: ChannelHandlerContext?) {
    TODO("Not yet implemented")
  }

  /**
   * The Channel of the ChannelHandlerContext was registered
   * is now inactive and reached its end of lifetime.
   */
  override fun channelInactive(ctx: ChannelHandlerContext?) {
    TODO("Not yet implemented")
  }

  /**
   * Invoked when the current Channel has read a message from the peer.
   */
  override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    when (msg) {
      is Authentication -> onAuthentication(ctx, msg)
      is InvalidPacket -> onInvalidPacket(ctx, msg)
      is SyncingPacket -> onSyncingPacket(ctx, msg)
    }
  }

  /**
   * Invoked when the last message read by the current read operation
   * has been consumed by channelRead. If ChannelOption.AUTO_READ is
   * off, no further attempt to read an inbound data from the current
   * Channel will be made until ChannelHandlerContext.read is called.
   */
  override fun channelReadComplete(ctx: ChannelHandlerContext?) {
    // Do Nothing
  }

  /**
   * Gets called if an user event was triggered.
   */
  override fun userEventTriggered(ctx: ChannelHandlerContext?, evt: Any?) {
    // Do Nothing
  }

  /**
   * Gets called once the writable state of a Channel changed. You
   * can check the state with Channel.isWritable
   */
  override fun channelWritabilityChanged(ctx: ChannelHandlerContext?) {
    // Do Nothing
  }
}
