package com.srilakshmikanthanp.clipbirdroid.syncing.lan.server

import android.content.Context
import android.util.Log
import com.srilakshmikanthanp.clipbirdroid.common.enums.AuthStatus
import com.srilakshmikanthanp.clipbirdroid.common.enums.PingType
import com.srilakshmikanthanp.clipbirdroid.common.trust.ClipbirdTrustManager
import com.srilakshmikanthanp.clipbirdroid.common.types.Device
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.constant.appMaxIdleReadTime
import com.srilakshmikanthanp.clipbirdroid.constant.appMaxIdleWriteTime
import com.srilakshmikanthanp.clipbirdroid.mdns.MdnsRegister
import com.srilakshmikanthanp.clipbirdroid.mdns.RegisterListener
import com.srilakshmikanthanp.clipbirdroid.packets.Authentication
import com.srilakshmikanthanp.clipbirdroid.packets.PingPacket
import com.srilakshmikanthanp.clipbirdroid.packets.SyncingItem
import com.srilakshmikanthanp.clipbirdroid.packets.SyncingPacket
import com.srilakshmikanthanp.clipbirdroid.store.Storage
import com.srilakshmikanthanp.clipbirdroid.syncing.lan.AuthenticationEncoder
import com.srilakshmikanthanp.clipbirdroid.syncing.lan.InvalidPacketEncoder
import com.srilakshmikanthanp.clipbirdroid.syncing.SyncRequestHandler
import com.srilakshmikanthanp.clipbirdroid.syncing.lan.PacketDecoder
import com.srilakshmikanthanp.clipbirdroid.syncing.lan.PingPacketEncoder
import com.srilakshmikanthanp.clipbirdroid.syncing.Synchronizer
import com.srilakshmikanthanp.clipbirdroid.syncing.lan.SyncingPacketEncoder
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.ssl.ClientAuth
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslHandler
import io.netty.handler.ssl.SslHandshakeCompletionEvent
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import io.netty.handler.timeout.IdleStateHandler
import io.netty.util.AttributeKey
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import java.net.InetSocketAddress
import java.security.cert.X509Certificate

/**
 * A SSl Server Using Netty as a Backend
 */
class Server(private val context: Context) : ChannelInboundHandler, RegisterListener, Synchronizer {
  // Client State Change Handlers
  private val clientStateChangeHandlers = mutableListOf<OnClientStateChangeHandler>()

  fun interface OnClientStateChangeHandler {
    fun onClientStateChanged(device: Device, connected: Boolean)
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

  // Server State Change Handlers
  private val mdnsRegisterStatusChangeHandlers = mutableListOf<OnMdnsRegisterStatusChangeHandler>()

  fun interface OnMdnsRegisterStatusChangeHandler {
    fun onMdnsRegisterStatusChanged(registered: Boolean)
  }

  /**
   * Add Server State Change Handler
   */
  fun addMdnsRegisterStatusChangeHandler(handler: OnMdnsRegisterStatusChangeHandler) {
    mdnsRegisterStatusChangeHandlers.add(handler)
  }

  /**
   * Remove Server State Change Handler
   */
  fun removeMdnsRegisterStatusChangeHandler(handler: OnMdnsRegisterStatusChangeHandler) {
    mdnsRegisterStatusChangeHandlers.remove(handler)
  }

  private val mdnsServiceRegisterFailedHandlers = mutableListOf<OnMdnsServiceRegisterFailedHandler>()

  fun interface OnMdnsServiceRegisterFailedHandler {
    fun onServiceRegistrationFailed(errorCode: Int)
  }

  fun addMdnsServiceRegisterFailedHandler(handler: OnMdnsServiceRegisterFailedHandler) {
    mdnsServiceRegisterFailedHandlers.add(handler)
  }

  fun removeMdnsServiceRegisterFailedHandler(handler: OnMdnsServiceRegisterFailedHandler) {
    mdnsServiceRegisterFailedHandlers.remove(handler)
  }

  private val mdnsServiceUnregisterFailedHandlers = mutableListOf<OnMdnsServiceUnregisterFailedHandler>()

  fun interface OnMdnsServiceUnregisterFailedHandler {
    fun onServiceUnregistrationFailed(errorCode: Int)
  }

  fun addMdnsServiceUnregisterFailedHandler(handler: OnMdnsServiceUnregisterFailedHandler) {
    mdnsServiceUnregisterFailedHandlers.add(handler)
  }

  fun removeMdnsServiceUnregisterFailedHandler(handler: OnMdnsServiceUnregisterFailedHandler) {
    mdnsServiceUnregisterFailedHandlers.remove(handler)
  }

  // Auth Request Handlers
  private val authRequestHandlers = mutableListOf<OnAuthRequestHandler>()

  fun interface OnAuthRequestHandler {
    fun onAuthRequest(client: Device)
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

  // Sync Request Handlers
  private val syncRequestHandlers = mutableListOf<SyncRequestHandler>()

  /**
   * Add Sync Request Handler
   */
  override fun addSyncRequestHandler(handler: SyncRequestHandler) {
    syncRequestHandlers.add(handler)
  }

  /**
   * Remove Sync Request Handler
   */
  override fun removeSyncRequestHandler(handler: SyncRequestHandler) {
    syncRequestHandlers.remove(handler)
  }

  // Client List Change Handlers
  private val clientListChangeHandlers = mutableListOf<OnClientListChangeHandler>()

  fun interface OnClientListChangeHandler {
    fun onClientListChanged(clients: List<Device>)
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

  // Channel Initializer
  inner class NewChannelInitializer : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
      // get the Idle read and write time from constants
      val (r, w) = Pair(appMaxIdleReadTime(), appMaxIdleWriteTime())

      // create SSL Context from cert and private key
      val sslContext = SslContextBuilder.forServer(sslCert?.first, sslCert?.second)
        .trustManager(ClipbirdTrustManager())
        .clientAuth(ClientAuth.REQUIRE)
        .build()

      // Idle state Handler
      ch.pipeline().addLast(IdleStateHandler(r, w, 0))

      // SSL
      ch.pipeline().addLast(sslContext?.newHandler(ch.alloc()))

      // Encoder
      ch.pipeline().addLast(AuthenticationEncoder())
      ch.pipeline().addLast(InvalidPacketEncoder())
      ch.pipeline().addLast(PingPacketEncoder())
      ch.pipeline().addLast(SyncingPacketEncoder())

      // Decoder
      ch.pipeline().addLast(PacketDecoder())

      // Add the Server Handler
      ch.pipeline().addLast(this@Server)
    }
  }

  // List of clients unauthenticated
  private val unauthenticatedClients = mutableListOf<ChannelHandlerContext>()

  // List of clients authenticated
  private val authenticatedClients = mutableListOf<ChannelHandlerContext>()

  // Device Name Attribute Key
  private val deviceName = AttributeKey.valueOf<String>("DEVICE_NAME")

  // Netty's SSL server Instance
  private var sslServer: Channel? = null

  // Register instance
  private val mdnsRegister = MdnsRegister(context)

  // Ssl Context for the Server
  private var sslCert: SSLConfig? = null

  // TAG for logging
  companion object {
    const val TAG = "Server"
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
  private fun notifyMdnsRegisterStatusChangeHandlers(started: Boolean) {
    mdnsRegisterStatusChangeHandlers.forEach {
      it.onMdnsRegisterStatusChanged(started)
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

  fun notifyMdnsServiceRegisterFailedHandlers(errorCode: Int) {
    mdnsServiceRegisterFailedHandlers.forEach {
      it.onServiceRegistrationFailed(errorCode)
    }
  }

  fun notifyMdnsServiceUnregisterFailedHandlers(errorCode: Int) {
    mdnsServiceUnregisterFailedHandlers.forEach {
      it.onServiceUnregistrationFailed(errorCode)
    }
  }

  /**
   * Handle the SSl Hand shake Complete
   */
  private fun onSSLHandShakeComplete(ctx: ChannelHandlerContext, evt: SslHandshakeCompletionEvent) {
    // if handshake is not completed
    if (!evt.isSuccess) ctx.close().also { return }

    // get the Handler for SSL from Pipeline
    val ssl = ctx.channel().pipeline().get(SslHandler::class.java) as SslHandler

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

    // put name to ctx extra
    ctx.channel().attr(deviceName).set(name)

    // Add to unauthenticated clients
    unauthenticatedClients.add(ctx)

    // if Storage dis not have name
    if (!storage.hasClientCert(name)) {
      return this.notifyAuthRequestHandlers(Device(addr.address, addr.port, name))
    }

    // get cert for name
    val cert = storage.getClientCert(name)

    // if matches then connect
    if (peerCert == cert) {
      return this.onClientAuthenticated(Device(addr.address, addr.port, name))
    }

    // Notify Auth Request Handlers
    this.notifyAuthRequestHandlers(Device(addr.address, addr.port, name))
  }

  /**
   * on Idle State Event
   */
  private fun onIdleStateEvent(ctx: ChannelHandlerContext, evt: IdleStateEvent) {
    // if it is not in the authenticated clients
    if (!authenticatedClients.contains(ctx)) return

    if (evt.state() == IdleState.WRITER_IDLE) {
      ctx.writeAndFlush(PingPacket(PingType.Ping))
    }

    if (evt.state() == IdleState.READER_IDLE) {
      ctx.close()
    }
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
   * On Ping Packet
   */
  private fun onPingPacket(ctx: ChannelHandlerContext, m: PingPacket) {
    if (m.getPingType() == PingType.Ping) {
      ctx.writeAndFlush(PingPacket(PingType.Pong))
    }

    Log.i(TAG, "Ping: ${m.getPingType()}")
  }

  /**
   * On Syncing Packet Received
   */
  private fun onSyncingPacket(ctx: ChannelHandlerContext, m: SyncingPacket) {
    // list of items to be synced
    val items = mutableListOf<Pair<String, ByteArray>>()

    // add the items to the list
    for (i in m.getItems()) {
      if (i.getPayloadLength() != 0) items.add(Pair(String(i.getMimeType()), i.getPayload()))
    }

    // if list is empty
    if (items.isEmpty()) return

    // notify the sync handlers
    notifySyncRequestHandlers(items)

    // send to all client except this one
    this.sendPacketToAllClients(m, ctx)
  }


  /**
   * Initialize the Server
   */
  init {
    mdnsRegister.addRegisterListener(this)
  }

  /**
   * Is server started
   */
  fun isRegistered(): Boolean {
    return mdnsRegister.isRegistered()
  }

  /**
   * Is Server Running
   */
  fun isRunning(): Boolean {
    return sslServer != null && sslCert != null && sslServer?.isOpen == true
  }

  /**
   * Start the Server
   */
  fun startServer() {
    val workerGroup = NioEventLoopGroup()
    val bossGroup = NioEventLoopGroup()
    val server = ServerBootstrap()
     .channel(NioServerSocketChannel::class.java)
     .group(bossGroup, workerGroup)
     .childHandler(NewChannelInitializer())
     .childOption(ChannelOption.SO_KEEPALIVE, true)
     .bind(0)
     .sync()
     .channel()

    val addr = server.localAddress() as InetSocketAddress
    mdnsRegister.registerService(addr.port)

    this.sslServer = server
  }

  /**
   * Register the Service
   */
  fun registerService() {
    if (!this.isRunning()) throw RuntimeException("Server is not started")
    val addr = sslServer?.localAddress() as InetSocketAddress
    mdnsRegister.registerService(addr.port)
  }

  /**
   * Stop the Server
   */
  fun stopServer() {
    if (!this.isRunning()) return

    for (client in unauthenticatedClients) {
      client.close()
    }

    for (client in authenticatedClients) {
      client.close()
    }

    sslServer?.close()?.sync()
    this.sslServer = null
    mdnsRegister.unRegisterService()
  }

  /**
   * Un register the Service
   */
  fun unregisterService() {
    if (!this.isRunning()) throw RuntimeException("Server is not started")
    mdnsRegister.unRegisterService()
  }

  fun reRegisterService() {
    if (!this.isRunning()) throw RuntimeException("Server is not started")
    val addr = sslServer?.localAddress() as InetSocketAddress
    mdnsRegister.reRegister(addr.port)
  }

  /**
   * Set the SSL certificate and key
   */
  fun setSslConfig(sslCert: SSLConfig) {
    if (this.isRunning()) {
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
  override fun synchronize(items: List<Pair<String, ByteArray>>) {
    // if server is not running the throw error
    if (!this.isRunning()) throw RuntimeException("Server is not started")

    // create the syncing Items
    val syncingItems = items.map {
      return@map SyncingItem(
        it.first.toByteArray(),
        it.second
      )
    }

    // Array
    val array = syncingItems.toTypedArray()

    // send the packet to all the clients
    sendPacketToAllClients(SyncingPacket(array))
  }

  /**
   * Get the Unauthenticated Clients
   */
  fun getUnauthenticatedClients(): List<Device> {
    return unauthenticatedClients.map {
      val addr = it.channel().remoteAddress() as InetSocketAddress
      val name = it.channel().attr(deviceName).get()
      Device(addr.address, addr.port, name)
    }
  }

  /**
   * Get the List of Clients
   */
  fun getClients(): List<Device> {
    return authenticatedClients.map {
      val addr = it.channel().remoteAddress() as InetSocketAddress
      val name = it.channel().attr(deviceName).get()
      Device(addr.address, addr.port, name)
    }
  }

  /**
   * Disconnect the client from the server and delete the client
   */
  fun disconnectClient(client: Device) {
    (getChannel(client) ?: throw RuntimeException("Client not found")).close()
  }

  /**
   * Disconnect the all the clients from the server
   */
  fun disconnectAllClients() {
    // close all the clients
    for (client in authenticatedClients) {
      client.close()
    }

    // clear the list
    authenticatedClients.clear()
  }

  /**
   * Get the Server Details
   */
  fun getServerInfo(): Device {
    // if server is not running the throw error
    if (!this.isRunning()) throw RuntimeException("Server is not started")

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
    val sslHandler = ctx.channel().pipeline().get(SslHandler::class.java) as SslHandler
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
      (addr.address == client.ip) && (addr.port == client.port)
    } ?: return

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
      (addr.address == client.ip) && (addr.port == client.port)
    } ?: return

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
  @Deprecated("Deprecated in Java")
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
    // Do Nothing
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
    val name = ctx.channel().attr(deviceName).get()

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
    if (!authenticatedClients.contains(ctx)) { return }

    when (msg) {
      is SyncingPacket -> return onSyncingPacket(ctx, msg)
      is PingPacket -> return onPingPacket(ctx, msg)
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
  override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any?) {
    // check if event is SSL Handshake Completed
    if (evt is SslHandshakeCompletionEvent) {
      this.onSSLHandShakeComplete(ctx, evt)
    }

    // Check if it is an IdleState Event
    if (evt is IdleStateEvent) {
      this.onIdleStateEvent(ctx, evt)
    }
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
    notifyMdnsRegisterStatusChangeHandlers(false)
  }

  /**
   * Gets called if NSD service is registered
   */
  override fun onServiceRegistered() {
    notifyMdnsRegisterStatusChangeHandlers(true)
  }

  override fun onServiceRegistrationFailed(errorCode: Int) {
    notifyMdnsServiceRegisterFailedHandlers(errorCode)
  }

  override fun onServiceUnregistrationFailed(errorCode: Int) {
    notifyMdnsServiceUnregisterFailedHandlers(errorCode)
  }
}
