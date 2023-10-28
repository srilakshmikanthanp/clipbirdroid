package com.srilakshmikanthanp.clipbirdroid.network.syncing

import android.content.Context
import android.util.Log
import com.srilakshmikanthanp.clipbirdroid.common.ClipbirdTrustManager
import com.srilakshmikanthanp.clipbirdroid.intface.OnAuthRequestHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnClientListChangeHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnClientStateChangeHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnServerStateChangeHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnSyncRequestHandler
import com.srilakshmikanthanp.clipbirdroid.network.packets.Authentication
import com.srilakshmikanthanp.clipbirdroid.network.packets.SyncingItem
import com.srilakshmikanthanp.clipbirdroid.network.packets.SyncingPacket
import com.srilakshmikanthanp.clipbirdroid.network.service.mdns.Register
import com.srilakshmikanthanp.clipbirdroid.network.syncing.common.AuthenticationEncoder
import com.srilakshmikanthanp.clipbirdroid.network.syncing.common.InvalidPacketEncoder
import com.srilakshmikanthanp.clipbirdroid.network.syncing.common.PacketDecoder
import com.srilakshmikanthanp.clipbirdroid.network.syncing.common.SyncingPacketEncoder
import com.srilakshmikanthanp.clipbirdroid.store.Storage
import com.srilakshmikanthanp.clipbirdroid.types.aliases.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.types.device.Device
import com.srilakshmikanthanp.clipbirdroid.types.enums.AuthStatus
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandler
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslHandler
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import java.net.InetSocketAddress
import java.security.cert.X509Certificate

/**
 * A SSl Server Using Netty as a Backend
 */
class Server(private val context: Context) : ChannelInboundHandler, Register.RegisterListener {
  // Client State Change Handlers
  private val clientStateChangeHandlers = mutableListOf<OnClientStateChangeHandler>()

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


  // Server State Change Handlers
  private val serverStateChangeHandlers = mutableListOf<OnServerStateChangeHandler>()

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


  // Auth Request Handlers
  private val authRequestHandlers = mutableListOf<OnAuthRequestHandler>()

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

  // Sync Request Handlers
  private val syncRequestHandlers = mutableListOf<OnSyncRequestHandler>()

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

  // Client List Change Handlers
  private val clientListChangeHandlers = mutableListOf<OnClientListChangeHandler>()

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

  // List of clients unauthenticated
  private val unauthenticatedClients = mutableListOf<ChannelHandlerContext>()

  // List of clients authenticated
  private val authenticatedClients = mutableListOf<ChannelHandlerContext>()

  // Filter for the Server
  inner class ChannelsFilter : ChannelInboundHandlerAdapter() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
      if (authenticatedClients.contains(ctx)) ctx.fireChannelRead(msg)
    }
  }

  // Channel Initializer
  inner class NewChannelInitializer : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
      // create SSL Context from cert and private key
      val sslContext = SslContextBuilder.forServer(sslCert?.first, sslCert?.second)
       .trustManager(ClipbirdTrustManager()).build()

      // Preprocessing Handlers
      ch.pipeline().addLast("ssl", sslContext?.newHandler(ch.alloc()))
      ch.pipeline().addLast(ChannelsFilter())
      ch.pipeline().addLast(AuthenticationEncoder())
      ch.pipeline().addLast(InvalidPacketEncoder())
      ch.pipeline().addLast(SyncingPacketEncoder())
      ch.pipeline().addLast(PacketDecoder())

      // Add the Server Handler
      ch.pipeline().addLast(this@Server)
    }
  }

  // Netty's SSL server Instance
  private var sslServer: Channel? = null

  // Register instance
  private val register = Register(context)

  // Ssl Context for the Server
  private var sslCert: SSLConfig? = null

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
    if (server == null) throw RuntimeException("Server Can't be SetUp")

    // check for the non null assertion
    this.sslServer = server

    // Address
    val addr = server.localAddress() as InetSocketAddress

    // register the service
    register.registerService(addr.port)
  }

  /**
   * Get Channel for the Device
   */
  private fun getChannel(device: Device): ChannelHandlerContext? {
    // find the channel in authenticated clients
    return authenticatedClients.find {
      val addr = it.channel().remoteAddress() as InetSocketAddress
      return@find addr.address == device.ip && addr.port == device.port
    }
  }

  /**
   * Send packet to all Clients except the one specified
   */
  private fun<T> sendPacketToAllClients(packet: T, except: ChannelHandlerContext? = null) {
    for (client in authenticatedClients) {
      if (client != except) client.writeAndFlush(packet)
    }
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

    // send to all client except this one
    this.sendPacketToAllClients(m, ctx)
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
    // if the server is already running
    if (this.isServerRunning()) throw RuntimeException("Server is already started")

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
    if (!this.isServerRunning()) throw RuntimeException("Server is not started")

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
  fun setSslConfig(sslCert: SSLConfig) {
    if (this.isServerRunning()) {
      throw RuntimeException("Server is already started")
    }

    this.sslCert = sslCert
  }

  /**
   * Get the SSL Context
   */
  fun getSslConfig(): SSLConfig? {
    return sslCert
  }

  /**
   * Sync the Items
   */
  fun syncItems(items: List<Pair<String, ByteArray>>) {
    // if server is not running the throw error
    if (!this.isServerRunning()) throw RuntimeException("Server is not started")

    // create the syncing Items
    val syncingItems = items.map {
      return@map SyncingItem(it.first.toByteArray(), it.second)
    }

    // Array
    val array = syncingItems.toTypedArray()

    // send the packet to all the clients
    sendPacketToAllClients(SyncingPacket(array))
  }

  /**
   * Get the List of Clients
   */
  fun getClients(): List<Device> {
    return authenticatedClients.map {
      val addr = it.channel().remoteAddress() as InetSocketAddress
      val ssl = it.channel().pipeline().get("ssl") as SslHandler
      val cert = ssl.engine().session.peerCertificates[0] as X509Certificate
      val x500Name = JcaX509CertificateHolder(cert).subject
      val cn = x500Name.getRDNs(BCStyle.CN)[0]
      val name = IETFUtils.valueToString(cn.first.value)
      Device(addr.address, addr.port, name)
    }
  }

  /**
   * Disconnect the client from the server and delete the client
   */
  fun disconnectClient(client: Device) {
    (getChannel(client) ?: throw RuntimeException("Client not found")).close().sync()
  }

  /**
   * Disconnect the all the clients from the server
   */
  fun disconnectAllClients() {
    // close all the clients
    for (client in authenticatedClients) {
      client.close().sync()
    }

    // clear the list
    authenticatedClients.clear()
  }

  /**
   * Get the Server Details
   */
  fun getServerInfo(): Device {
    // if server is not running the throw error
    if (!this.isServerRunning()) throw RuntimeException("Server is not started")

    // Get the Required parameters
    val address = sslServer?.localAddress() as InetSocketAddress?
    val x500Name = JcaX509CertificateHolder(sslCert?.second).subject
    val cn = x500Name.getRDNs(BCStyle.CN)[0]
    val name = IETFUtils.valueToString(cn.first.value)

    // if it is null
    if (address == null || name == null) {
      throw RuntimeException("Server is not started")
    }

    // Construct Device and Return
    return Device(address.address, address.port, name)
  }

  /**
   * Get the Client Certificate
   */
  fun getClientCertificate(client: Device): X509Certificate {
    // Find the client in authenticated clients
    val ctx = getChannel(client) ?: throw RuntimeException("Client not found")

    // get the certificate
    val sslHandler = ctx.channel().pipeline().get("ssl") as SslHandler
    val cert = sslHandler.engine().session.peerCertificates[0]

    // return the certificate
    return cert as X509Certificate
  }

  /**
   * The function that is called when the client is authenticated
   */
  fun onClientAuthenticated(client: Device) {
    val ctx = unauthenticatedClients.find {
      val addr = it.channel().remoteAddress() as InetSocketAddress
      return@find addr.address == client.ip && addr.port == client.port
    } ?: throw RuntimeException("Client not found")

    // remove the client from unauthenticated clients
    unauthenticatedClients.remove(ctx)

    // add the client to authenticated clients
    authenticatedClients.add(ctx)

    // notify the client state change handlers
    notifyClientStateChangeHandlers(client, true)

    // notify the client list change handlers
    notifyClientListChangeHandlers(getClients())

    // packet
    val packet = Authentication(AuthStatus.AuthOkay)

    // send the packet to the client
    ctx.writeAndFlush(packet)
  }

  /**
   * The function that is called when the client is not authenticated
   */
  fun onClientNotAuthenticated(client: Device) {
    val ctx = unauthenticatedClients.find {
      val addr = it.channel().remoteAddress() as InetSocketAddress
      return@find addr.address == client.ip && addr.port == client.port
    } ?: throw RuntimeException("Client not found")

    // remove the client from unauthenticated clients
    unauthenticatedClients.remove(ctx)

    // send the packet to the client
    val fut = ctx.writeAndFlush(Authentication(AuthStatus.AuthFail))

    // close the channel
    fut.addListener { ctx.close() }
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
   * Gets called if a Throwable was thrown.
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
  override fun channelActive(ctx: ChannelHandlerContext) {
    // get the Handler for SSL from Pipeline
    val ssl = ctx.channel().pipeline().get("ssl") as SslHandler

    // get the Storage Instance
    val storage = Storage.getInstance(context)

    // check if client has certificate
    if(ssl.engine().session.peerCertificates.isEmpty()) {
      ctx.close().also { return }
    }

    // get the Peer Certificate
    val peerCert = ssl.engine().session.peerCertificates[0] as X509Certificate

    // get name
    val addr = ctx.channel().remoteAddress() as InetSocketAddress

    // get CN name from certificate using bouncy castle
    val x500Name = JcaX509CertificateHolder(peerCert).subject
    val rdns = x500Name.getRDNs(BCStyle.CN)

    // is does not have CN name
    if (rdns.isEmpty()) ctx.close().also { return }

    // get the CN name
    val name = IETFUtils.valueToString(rdns[0].first.value)

    // if Storage dis not have name
    if (!storage.hasClientCert(name)) {
      return this.notifyAuthRequestHandlers(Device(addr.address, addr.port, name))
    }

    // get cert for name
    val cert = storage.getClientCert(name)

    // if matches then connect
    if (peerCert == cert) {
      this.onClientAuthenticated(Device(addr.address, addr.port, name))
    }

    // Notify Auth Request Handlers
    this.notifyAuthRequestHandlers(Device(addr.address, addr.port, name))
  }

  /**
   * The Channel of the ChannelHandlerContext was registered
   * is now inactive and reached its end of lifetime.
   */
  override fun channelInactive(ctx: ChannelHandlerContext) {
    // if it is in the unauthenticated clients
    if (unauthenticatedClients.remove(ctx)) return

    // if it is not in the authenticated clients
    if (!authenticatedClients.remove(ctx)) return

    // get the address, port, name
    val addr = ctx.channel().remoteAddress() as InetSocketAddress
    val ssl = ctx.channel().pipeline().get("ssl") as SslHandler
    val cert = ssl.engine().session.peerCertificates[0] as X509Certificate
    val x500Name = JcaX509CertificateHolder(cert).subject
    val cn = x500Name.getRDNs(BCStyle.CN)[0]
    val name = IETFUtils.valueToString(cn.first.value)

    // create a device for context
    val device = Device(addr.address, addr.port, name)

    // notify the client state change handlers
    notifyClientStateChangeHandlers(device, false)

    // notify the client list change handlers
    notifyClientListChangeHandlers(getClients())
  }

  /**
   * Invoked when the current Channel has read a message from the peer.
   */
  override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    when (msg) {
      is SyncingPacket -> return onSyncingPacket(ctx, msg)
    }
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
