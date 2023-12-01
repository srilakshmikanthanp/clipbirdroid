package com.srilakshmikanthanp.clipbirdroid.network.syncing

import android.content.Context
import android.util.Log
import com.srilakshmikanthanp.clipbirdroid.common.trust.ClipbirdTrustManager
import com.srilakshmikanthanp.clipbirdroid.constant.appMaxIdleReadTime
import com.srilakshmikanthanp.clipbirdroid.constant.appMaxIdleWriteTime
import com.srilakshmikanthanp.clipbirdroid.intface.OnConnectionErrorHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnInvalidPacketHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnServerFoundHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnServerGoneHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnServerListChangeHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnServerStatusChangeHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnSyncRequestHandler
import com.srilakshmikanthanp.clipbirdroid.network.packets.Authentication
import com.srilakshmikanthanp.clipbirdroid.network.packets.InvalidPacket
import com.srilakshmikanthanp.clipbirdroid.network.packets.PingPacket
import com.srilakshmikanthanp.clipbirdroid.network.packets.SyncingItem
import com.srilakshmikanthanp.clipbirdroid.network.packets.SyncingPacket
import com.srilakshmikanthanp.clipbirdroid.network.service.Browser
import com.srilakshmikanthanp.clipbirdroid.network.syncing.common.AuthenticationEncoder
import com.srilakshmikanthanp.clipbirdroid.network.syncing.common.InvalidPacketEncoder
import com.srilakshmikanthanp.clipbirdroid.network.syncing.common.PacketDecoder
import com.srilakshmikanthanp.clipbirdroid.network.syncing.common.PingPacketEncoder
import com.srilakshmikanthanp.clipbirdroid.network.syncing.common.SyncingPacketEncoder
import com.srilakshmikanthanp.clipbirdroid.store.Storage
import com.srilakshmikanthanp.clipbirdroid.types.aliases.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.types.device.Device
import com.srilakshmikanthanp.clipbirdroid.types.enums.AuthStatus
import com.srilakshmikanthanp.clipbirdroid.types.enums.PingType
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandler
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
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
 * Client Class for Syncing the Clipboard
 */
open class Client(private val context: Context): Browser.BrowserListener, ChannelInboundHandler {
  // List handlers for server list changed
  private val onServerListChangeHandlers = mutableListOf<OnServerListChangeHandler>()

  // Add handler for server list changed
  fun addServerListChangeHandler(handler: OnServerListChangeHandler) {
    onServerListChangeHandlers.add(handler)
  }

  // Remove handler for server list changed
  fun removeServerListChangeHandler(handler: OnServerListChangeHandler) {
    onServerListChangeHandlers.remove(handler)
  }

  // List handlers for server found
  private val onServerFoundHandlers = mutableListOf<OnServerFoundHandler>()

  // Add handler for server found
  fun addServerFoundHandler(handler: OnServerFoundHandler) {
    onServerFoundHandlers.add(handler)
  }

  // Remove handler for server found
  fun removeServerFoundHandler(handler: OnServerFoundHandler) {
    onServerFoundHandlers.remove(handler)
  }

  // List handlers for server gone
  private val onServerGoneHandlers = mutableListOf<OnServerGoneHandler>()

  // Add handler for server gone
  fun addServerGoneHandler(handler: OnServerGoneHandler) {
    onServerGoneHandlers.add(handler)
  }

  // Remove handler for server gone
  fun removeServerGoneHandler(handler: OnServerGoneHandler) {
    onServerGoneHandlers.remove(handler)
  }

  // List handlers for server status changed
  private val onServerStatusChangeHandlers = mutableListOf<OnServerStatusChangeHandler>()

  // Add handler for server status changed
  fun addServerStatusChangeHandler(handler: OnServerStatusChangeHandler) {
    onServerStatusChangeHandlers.add(handler)
  }

  // Remove handler for server status changed
  fun removeServerStatusChangeHandler(handler: OnServerStatusChangeHandler) {
    onServerStatusChangeHandlers.remove(handler)
  }

  // list handler for invalid packet
  private val onInvalidPacketHandlers = mutableListOf<OnInvalidPacketHandler>()

  // Add handler for invalid packet
  fun addInvalidPacketHandler(handler: OnInvalidPacketHandler) {
    onInvalidPacketHandlers.add(handler)
  }

  // Remove handler for invalid packet
  fun removeInvalidPacketHandler(handler: OnInvalidPacketHandler) {
    onInvalidPacketHandlers.remove(handler)
  }

  // List handlers for connection error
  private val onConnectionErrorHandlers = mutableListOf<OnConnectionErrorHandler>()

  // Add handler for connection error
  fun addConnectionErrorHandler(handler: OnConnectionErrorHandler) {
    onConnectionErrorHandlers.add(handler)
  }

  // Remove handler for connection error
  fun removeConnectionErrorHandler(handler: OnConnectionErrorHandler) {
    onConnectionErrorHandlers.remove(handler)
  }

  // List handlers for sync request
  private val onSyncRequestHandlers = mutableListOf<OnSyncRequestHandler>()

  // Add handler for sync request
  fun addSyncRequestHandler(handler: OnSyncRequestHandler) {
    onSyncRequestHandlers.add(handler)
  }

  // Remove handler for sync request
  fun removeSyncRequestHandler(handler: OnSyncRequestHandler) {
    onSyncRequestHandlers.remove(handler)
  }

  // TAG for logging
  companion object {
    val TAG = "Client"
  }

  // SSL Verifier Secured
  inner class SSLVerifierSecured: ChannelInboundHandlerAdapter() {
    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
      // check if event is SSL Handshake Completed
      if (evt !is SslHandshakeCompletionEvent) { return super.userEventTriggered(ctx, evt) }

      // if handshake is not completed
      if (!evt.isSuccess) ctx.close().also { return }

      // get the Handler for SSL from Pipeline
      val ssl = ctx.channel().pipeline().get(SslHandler::class.java) as SslHandler

      // get the Storage Instance
      val storage = Storage.getInstance(context)

      // check if peer has a valid certificate
      if (ssl.engine().session.peerCertificates.isEmpty()) {
        ctx.close().also { return }
      }

      // get the peer certificate
      val peerCert = ssl.engine().session.peerCertificates[0] as X509Certificate

      // get CN name from certificate using bouncy castle
      val x500Name = JcaX509CertificateHolder(peerCert).subject
      val rdns = x500Name.getRDNs(BCStyle.CN)

      // is does not have CN name
      if (rdns.isEmpty()) {
        ctx.close().also { return }
      }

      // get the CN Name
      val name = IETFUtils.valueToString(rdns[0].first.value)

      // check is storage has certificate for name
      if (!storage.hasServerCert(name)) {
        ctx.close().also { return }
      }

      // get the certificate from storage
      val cert = storage.getServerCert(name)!!

      // check if two certificates are same
      if (cert != peerCert) ctx.close().also { return }

      // Next Handler
      ctx.fireUserEventTriggered(evt)
    }
  }

  // SSL verifier
  inner class SSLVerifier : ChannelInboundHandlerAdapter() {
    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
      // check if event is SSL Handshake Completed
      if (evt !is SslHandshakeCompletionEvent) { return super.userEventTriggered(ctx, evt) }

      // if handshake is not completed
      if (!evt.isSuccess) ctx.close().also { return }

      // get the Handler for SSL from Pipeline
      val ssl = ctx.channel().pipeline().get(SslHandler::class.java) as SslHandler

      // check if peer has a valid certificate
      if (ssl.engine().session.peerCertificates.isEmpty()) {
        ctx.close().also { return }
      }

      // get the certificate
      val cert = ssl.engine().session.peerCertificates[0] as X509Certificate

      // get CN name from certificate using bouncy castle
      val x500Name = JcaX509CertificateHolder(cert).subject
      val rdns = x500Name.getRDNs(BCStyle.CN)

      // is does not have CN name
      if (rdns.isEmpty()) ctx.close().also { return }

      // Next Handler
      ctx.fireUserEventTriggered(evt)
    }
  }

  // Channel Initializer Secured
  inner class InitializerSecured: ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
      // get the Idle read and write time from constants
      val (r, w) = Pair(appMaxIdleReadTime(), appMaxIdleWriteTime())

      // create SSL Context from cert and private key
      val sslContext = SslContextBuilder.forClient()
        .keyManager(sslConfig?.first, sslConfig?.second)
        .trustManager(ClipbirdTrustManager()).build()

      // Idle State
      ch.pipeline().addLast(IdleStateHandler(r, w, 0))

      // SSL
      ch.pipeline().addLast(sslContext.newHandler(ch.alloc()))
      ch.pipeline().addLast(SSLVerifierSecured())

      // Encoder
      ch.pipeline().addLast(AuthenticationEncoder())
      ch.pipeline().addLast(InvalidPacketEncoder())
      ch.pipeline().addLast(PingPacketEncoder())
      ch.pipeline().addLast(SyncingPacketEncoder())

      // Decoder
      ch.pipeline().addLast(PacketDecoder())

      // Add the Server Handler
      ch.pipeline().addLast(this@Client)
    }
  }

  // Channel Initializer
  inner class Initializer: ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
      // get the Idle read and write time from constants
      val (r, w) = Pair(appMaxIdleReadTime(), appMaxIdleWriteTime())

      // create SSL Context from cert and private key
      val sslContext = SslContextBuilder.forClient()
        .keyManager(sslConfig?.first, sslConfig?.second)
        .trustManager(ClipbirdTrustManager()).build()

      // Idle
      ch.pipeline().addLast(IdleStateHandler(r, w, 0))

      // SSL
      ch.pipeline().addLast(sslContext.newHandler(ch.alloc()))
      ch.pipeline().addLast(SSLVerifier())

      // Encoder
      ch.pipeline().addLast(AuthenticationEncoder())
      ch.pipeline().addLast(InvalidPacketEncoder())
      ch.pipeline().addLast(PingPacketEncoder())
      ch.pipeline().addLast(SyncingPacketEncoder())

      // Decoder
      ch.pipeline().addLast(PacketDecoder())

      // Add the Server Handler
      ch.pipeline().addLast(this@Client)
    }
  }

  // Device Name Attribute Key
  private val DEVICE_NAME = AttributeKey.valueOf<String>("DEVICE_NAME")

  // Ssl configuration
  private var sslConfig: SSLConfig? = null;

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
      handler.onServerListChanged(servers)
    }
  }

  /**
   * Notify all the listeners for server found
   */
  private fun notifyServerFound(server: Device) {
    for (handler in onServerFoundHandlers) {
      handler.onServerFound(server)
    }
  }

  /**
   * Notify all the listeners for server gone
   */
  private fun notifyServerGone(server: Device) {
    for (handler in onServerGoneHandlers) {
      handler.onServerGone(server)
    }
  }

  /**
   * Notify all the listeners for server status changed
   */
  private fun notifyServerStatusChanged(isConnected: Boolean, server: Device) {
    for (handler in onServerStatusChangeHandlers) {
      handler.onServerStatusChanged(isConnected, server)
    }
  }

  /**
   * Notify all the listeners for connection error
   */
  private fun notifyConnectionError(error: String) {
    for (handler in onConnectionErrorHandlers) {
      handler.onConnectionError(error)
    }
  }

  /**
   * Notify all the listeners for sync request
   */
  private fun notifySyncRequest(items: List<Pair<String, ByteArray>>) {
    for (handler in onSyncRequestHandlers) {
      handler.onSyncRequest(items)
    }
  }

  /**
   * Notify all the listeners for invalid packet
   */
  private fun notifyInvalidPacket(code: Int, message: String) {
    for (handler in onInvalidPacketHandlers) {
      handler.onInvalidPacket(code, message)
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

    // check if client has certificate
    if(ssl.engine().session.peerCertificates.isEmpty()) {
      ctx.close().also { return }
    }

    // get the Peer Certificate
    val peerCert = ssl.engine().session.peerCertificates[0] as X509Certificate

    // get CN name from certificate using bouncy castle
    val x500Name = JcaX509CertificateHolder(peerCert).subject
    val rdns = x500Name.getRDNs(BCStyle.CN)

    // is does not have CN name
    if (rdns.isEmpty()) ctx.close().also { return }

    // get the CN name
    val name = IETFUtils.valueToString(rdns[0].first.value)

    // put name to ctx extra
    ctx.channel().attr(DEVICE_NAME).set(name)

    // is already connected
    if (this.channel != null) {
      this.channel!!.close()
    }

    // set the channel
    this.channel = ctx.channel()
  }

  /**
   * on Idle State Event
   */
  private fun onIdleStateEvent(ctx: ChannelHandlerContext, evt: IdleStateEvent) {
    if (evt.state() == IdleState.WRITER_IDLE) {
      ctx.writeAndFlush(PingPacket(PingType.Ping))
    }

    if (evt.state() == IdleState.READER_IDLE) {
      ctx.close()
    }
  }

  /**
   * Process the Authentication Packet
   */
  private fun onAuthentication(ctx: ChannelHandlerContext, m: Authentication) {
    if (m.getAuthStatus() == AuthStatus.AuthOkay) {
      notifyServerStatusChanged(true, this.getConnectedServer()!!)
    }
  }

  /**
   * Process the InvalidPacket
   */
  private fun onInvalidPacket(ctx: ChannelHandlerContext, m: InvalidPacket) {
    this.notifyInvalidPacket(m.getErrorCode().value, m.getErrorMessage().toString())
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
   * Process the SyncingPacket
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
    notifySyncRequest(items)
  }

  /**
   * Sent Packet to Server
   */
  private fun<T> sendPacket(packet: T) {
    // check if channel is initialized
    if (this.getConnectedServer() == null) {
      throw Exception("Channel is not initialized")
    }

    // send packet
    val fut = channel?.writeAndFlush(packet)

    // Add Listener
    fut?.addListener {
      if (!it.isSuccess) {
        notifyConnectionError(it.cause().message.toString())
      }
    }
  }

  /**
   * Init
   */
  init {
    this.browser.addListener(this)
  }

  /**
   * Set the ssl configuration
   */
  fun setSslConfig(sslConfig: SSLConfig) {
    this.sslConfig = sslConfig
  }

  /**
   * Get the ssl configuration
   */
  fun getSslConfig(): SSLConfig? {
    return this.sslConfig
  }

  /**
   * Sync the Items
   */
  fun syncItems(items: List<Pair<String, ByteArray>>) {
    // check if channel is initialized
    if (this.getConnectedServer() == null) {
      return
    }

    // create the syncing Items
    val syncingItems = items.map {
      return@map SyncingItem(it.first.toByteArray(), it.second)
    }

    // Array
    val array = syncingItems.toTypedArray()

    // send the packet
    try {
      this.sendPacket(SyncingPacket(array))
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
    }
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
    // if already connected to server disconnect
    if (this.channel != null) this.channel!!.close()

    // create a bootstrap for channel
    Bootstrap().group(NioEventLoopGroup())
      .channel(NioSocketChannel::class.java)
      .handler(InitializerSecured())
      .option(ChannelOption.SO_KEEPALIVE, true)
      .connect(server.ip, server.port)
  }

  /**
   * Connect to server
   */
  fun connectToServer(server: Device) {
    // if already connected to server disconnect
    if (this.channel != null) this.channel!!.close()

    // create a bootstrap for channel
    Bootstrap().group(NioEventLoopGroup())
      .channel(NioSocketChannel::class.java)
      .handler(Initializer())
      .connect(server.ip, server.port)
  }

  /**
   * Get the Connected Server
   */
  fun getConnectedServer(): Device? {
    if (this.channel == null || !this.channel!!.isActive) return null
    val addr = channel!!.remoteAddress() as InetSocketAddress
    val name = channel!!.attr(DEVICE_NAME).get()
    return Device(addr.address, addr.port, name)
  }

  /**
   * Disconnect from the server
   */
  fun disconnectFromServer() {
    if (this.channel == null) {
      throw RuntimeException("Not Connected to server")
    } else {
      channel!!.close()
    }
  }

  /**
   * Get the Connected server Certificate
   */
  fun getConnectedServerCertificate(): X509Certificate {
    if (this.channel == null) throw RuntimeException("Not Connected to server")
    val ssl = channel!!.pipeline().get(SslHandler::class.java) as SslHandler
    return ssl.engine().session.peerCertificates[0] as X509Certificate
  }

  /**
   * Start the browser
   */
  fun startBrowsing() {
    this.browser.start()
  }

  /**
   * Stop the browser
   */
  fun stopBrowsing() {
    this.browser.stop()
  }

  /**
   * Called when a service is lost.
   */
  override fun onServiceRemoved(device: Device) {
    if (!this.servers.contains(device)) {
      return
    }

    notifyServerGone(device)
    this.servers.remove(device)
    notifyServerListChanged()
  }

  /**
   * Called when a service is found.
   */
  override fun onServiceAdded(device: Device) {
    // if already added
    if (this.servers.contains(device)) {
      return
    }

    notifyServerFound(device)
    this.servers.add(device)
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
    // Do Nothing
  }

  /**
   * The Channel of the ChannelHandlerContext was registered
   * is now inactive and reached its end of lifetime.
   */
  override fun channelInactive(ctx: ChannelHandlerContext?) {
    val addr = channel!!.remoteAddress() as InetSocketAddress
    val name = channel!!.attr(DEVICE_NAME).get()
    val host = Device(addr.address, addr.port, name)
    notifyServerStatusChanged(false, host).also {
      channel = null
    }
  }

  /**
   * Invoked when the current Channel has read a message from the peer.
   */
  override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    when (msg) {
      is Authentication -> onAuthentication(ctx, msg)
      is InvalidPacket -> onInvalidPacket(ctx, msg)
      is PingPacket -> onPingPacket(ctx, msg)
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
   * Gets called once the writable state of a Channel changed. You
   * can check the state with Channel.isWritable
   */
  override fun channelWritabilityChanged(ctx: ChannelHandlerContext?) {
    // Do Nothing
  }
}
