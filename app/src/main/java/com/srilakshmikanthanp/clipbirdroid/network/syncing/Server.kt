package com.srilakshmikanthanp.clipbirdroid.network.syncing

import android.content.Context
import android.util.Log
import com.srilakshmikanthanp.clipbirdroid.network.packets.InvalidPacket
import com.srilakshmikanthanp.clipbirdroid.network.packets.SyncingPacket
import com.srilakshmikanthanp.clipbirdroid.network.service.mdns.Register
import com.srilakshmikanthanp.clipbirdroid.network.syncing.common.AuthenticationEncoder
import com.srilakshmikanthanp.clipbirdroid.network.syncing.common.InvalidPacketEncoder
import com.srilakshmikanthanp.clipbirdroid.network.syncing.common.PacketDecoder
import com.srilakshmikanthanp.clipbirdroid.network.syncing.common.SyncingPacketEncoder
import com.srilakshmikanthanp.clipbirdroid.types.device.Device
import com.srilakshmikanthanp.clipbirdroid.types.enums.ErrorCode
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.ssl.SslContextBuilder
import java.net.InetSocketAddress
import java.security.PrivateKey
import java.security.cert.X509Certificate

/**
 * Client State Change Handler
 */
fun interface OnClientStateChangeHandler {
  fun onClientStateChanged(device: Device, connected: Boolean)
}

/**
 * Server State Change Handler
 */
fun interface OnServerStateChangeHandler {
  fun onServerStateChanged(started: Boolean)
}

/**
 * Auth Request Handler
 */
fun interface OnAuthRequestHandler {
  fun onAuthRequest(client: Device)
}

/**
 * Sync Request Handler
 */
fun interface OnSyncRequestHandler {
  fun onSyncRequest(items: List<Pair<String, ByteArray>>)
}

/**
 * Client List Change Handler
 */
fun interface OnClientListChangeHandler {
  fun onClientListChanged(clients: List<Device>)
}

/**
 * A SSl Server Using Netty as a Backend
 */
class Server(private val context: Context) : ChannelInboundHandler, Register.RegisterListener {
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

  // List of clients unauthenticated
  private val unauthenticatedClients = mutableListOf<ChannelHandlerContext>()

  // List of clients authenticated
  private val authenticatedClients = mutableListOf<ChannelHandlerContext>()

  // Channel Initializer
  inner class NewChannelInitializer : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
      // create SSL Context from cert and private key
      val sslContext = SslContextBuilder.forServer(sslCert?.first, sslCert?.second).build()

      // Add the SSL Handler
      ch.pipeline().addLast(sslContext?.newHandler(ch.alloc()))

      // Add the Packet Encoder
      ch.pipeline().addLast(AuthenticationEncoder())

      // Add the Packet Encoder
      ch.pipeline().addLast(InvalidPacketEncoder())

      // Add the Packet Decoder
      ch.pipeline().addLast(PacketDecoder())

      // Add the Packet Encoder
      ch.pipeline().addLast(SyncingPacketEncoder())

      // Add the Server Handler
      ch.pipeline().addLast(this)
    }
  }

  // Netty's SSL server Instance
  private var sslServer: Channel? = null

  // Register instance
  private val register = Register(context)

  // Ssl Context for the Server
  private var sslCert: Pair<PrivateKey, X509Certificate>? = null

  // TAG for logging
  companion object {
    val TAG = "Server"
  }

  /**
   * Notify the Client State Change Handlers
   */
  private fun notifyClientStateChangeHandlers(device: Device, connected: Boolean) {
    clientStateChangeHandlers.forEach {
      it.onClientStateChanged(device, connected)
    }
  }

  /**
   * Notify the Server State Change Handlers
   */
  private fun notifyServerStateChangeHandlers(started: Boolean) {
    serverStateChangeHandlers.forEach {
      it.onServerStateChanged(started)
    }
  }

  /**
   * Notify the Auth Request Handlers
   */
  private fun notifyAuthRequestHandlers(client: Device) {
    authRequestHandlers.forEach {
      it.onAuthRequest(client)
    }
  }

  /**
   * Notify the Sync Request Handlers
   */
  private fun notifySyncRequestHandlers(items: List<Pair<String, ByteArray>>) {
    syncRequestHandlers.forEach {
      it.onSyncRequest(items)
    }
  }

  /**
   * Notify the Client List Change Handlers
   */
  private fun notifyClientListChangeHandlers(clients: List<Device>) {
    clientListChangeHandlers.forEach {
      it.onClientListChanged(clients)
    }
  }

  /**
   * Set Up the Server
   */
  private fun setUpServer(server: Channel?) {
    // if the server is null
    if (server == null) {
      throw RuntimeException("Server Can't be SetUp")
    }

    // check for the non null assertion
    this.sslServer = server

    // Address
    val addr = server.localAddress() as InetSocketAddress

    // register the service
    register.registerService(addr.port)
  }

  /**
   * On Syncing Packet Received
   */
  private fun onSyncingPacket(ctx: ChannelHandlerContext, m: SyncingPacket) {
    // list of items to be synced
    val items = mutableListOf<Pair<String, ByteArray>>()

    // add the items to the list
    for (item in m.getItems()) {
      items.add(Pair(String(item.getMimeType()), item.getPayload()))
    }

    // notify the sync handlers
    notifySyncRequestHandlers(items)
  }

  /**
   * Initialize the Server
   */
  init {
    register.addRegisterListener(this)
  }

  /**
   * Is Server Running
   */
  fun isServerRunning(): Boolean {
    return sslServer != null && sslCert != null && sslServer?.isOpen == true
  }

  /**
   * Start the Server
   */
  fun startServer() {
    // check for the non null assertion
    if (this.isServerRunning()) {
      throw RuntimeException("Server is already started Or SSL Context is not set")
    }

    // create the server
    val workerGroup = NioEventLoopGroup()
    val bossGroup = NioEventLoopGroup()

    // create the server
    val serverFuture = ServerBootstrap()
      .channel(NioServerSocketChannel::class.java)
      .group(bossGroup, workerGroup)
      .childHandler(NewChannelInitializer())
      .bind(0)

    // On Bind Completed
    serverFuture.addListener {
      this.setUpServer(serverFuture.channel())
    }
  }

  /**
   * Stop the Server
   */
  fun stopServer() {
    // check for the non null assertion
    if (!this.isServerRunning()) {
      throw RuntimeException("Server is not started")
    }

    // close the server
    val fut = sslServer?.closeFuture()

    // Assign Null Value
    this.sslServer = null

    // Add Listener for complete
    fut?.addListener {
      register.unRegisterService()
    }
  }

  /**
   * Set the SSL certificate and key
   */
  fun setSslConfig(sslCert: Pair<PrivateKey, X509Certificate>) {
    this.sslCert = sslCert
  }

  /**
   * Get the SSL Context
   */
  fun getSslConfig(): Pair<PrivateKey, X509Certificate>? {
    return sslCert
  }

  /**
   * Sync the Items
   */
  fun syncItems(items: List<Pair<String, ByteArray>>) {
  }

  /**
   * Get the List of Clients
   */
  fun getClients(): List<Device> {
    return listOf()
  }

  /**
   * Disconnect the client from the server and delete the client
   */
  fun disconnectClient(client: Device) {

  }

  /**
   * Disconnect the all the clients from the server
   */
  fun disconnectAllClients() {

  }

  /**
   * Get the Server Details
   */
  fun getServerInfo(): Device {
    // if server is not running the throw error
    if(!this.isServerRunning()) {
      throw RuntimeException("Server is not started")
    }

    // Get the Required parameters
    val address = sslServer?.localAddress() as InetSocketAddress?
    val name = sslCert?.second?.subjectDN?.name

    // if it is null
    if(address == null || name == null) {
      throw RuntimeException("Server is not started")
    }

    // Construct Device and Return
    return Device(
      ip    =  address.address,
      port  =  address.port,
      name  =  name
    )
  }

  /**
   * Get the Client Certificate
   */
  fun getClientCertificate(client: Device): X509Certificate {
    TODO("Not yet implemented")
  }

  /**
   * The function that is called when the client is authenticated
   */
  fun onClientAuthenticated(client: Device) {
  }

  /**
   * The function that is called when the client is not authenticated
   */
  fun onClientNotAuthenticated(client: Device) {
  }

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

  /**
   * Gets called after the ChannelHandler was added to the actual
   * context and it's ready to handle events.
   */
  override fun handlerAdded(ctx: ChannelHandlerContext?) {
    // Do Nothing
  }

  /**
   * Gets called after the ChannelHandler was removed from the
   * actual context and it doesn't handle events anymore.
   */
  override fun handlerRemoved(ctx: ChannelHandlerContext?) {
    // Do Nothing
  }

  /**
   * Gets called if a [Throwable] was thrown.
   */
  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    Log.e(TAG, "Exception Caught", cause); ctx.close()
  }

  /**
   * The Channel of the ChannelHandlerContext was registered
   * with its EventLoop
   */
  override fun channelRegistered(ctx: ChannelHandlerContext?) {
    // Do Nothing
  }

  /**
   * The Channel of the ChannelHandlerContext was
   * unregistered from its EventLoop
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
    // is the message is instance of SyncingPacket
    when (msg) {
      is SyncingPacket -> return onSyncingPacket(ctx, msg)
    }

    // Unknown packet
    val error = "Unknown Packet".toByteArray()
    val code = ErrorCode.InvalidPacket
    val packet = InvalidPacket(code, error)
    ctx.writeAndFlush(packet)
  }

  /**
   * Invoked when the last message read by the current read
   * operation has been consumed by channelRead. If
   * ChannelOption.AUTO_READ is off, no further attempt to
   * read an inbound data from the current Channel will be
   * made until ChannelHandlerContext.read is called.
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
   * Gets called once the writable state of a Channel changed.
   * You can check the state with Channel.isWritable
   */
  override fun channelWritabilityChanged(ctx: ChannelHandlerContext?) {
    // Do Nothing
  }

  /**
   * Gets called if NSD service is un registered
   */
  override fun onServiceUnregistered() {
    notifyServerStateChangeHandlers(false)
  }

  /**
   * Gets called if NSD service is registered
   */
  override fun onServiceRegistered() {
    notifyServerStateChangeHandlers(true)
  }
}
