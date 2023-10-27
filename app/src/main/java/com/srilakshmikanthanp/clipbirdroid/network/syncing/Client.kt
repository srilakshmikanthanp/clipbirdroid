package com.srilakshmikanthanp.clipbirdroid.network.syncing

import android.content.Context
import android.util.Log
import com.srilakshmikanthanp.clipbirdroid.common.ClipbirdTrustManager
import com.srilakshmikanthanp.clipbirdroid.network.packets.Authentication
import com.srilakshmikanthanp.clipbirdroid.network.packets.InvalidPacket
import com.srilakshmikanthanp.clipbirdroid.network.packets.SyncingItem
import com.srilakshmikanthanp.clipbirdroid.network.packets.SyncingPacket
import com.srilakshmikanthanp.clipbirdroid.network.service.mdns.Browser
import com.srilakshmikanthanp.clipbirdroid.network.syncing.common.AuthenticationEncoder
import com.srilakshmikanthanp.clipbirdroid.network.syncing.common.InvalidPacketEncoder
import com.srilakshmikanthanp.clipbirdroid.network.syncing.common.PacketDecoder
import com.srilakshmikanthanp.clipbirdroid.network.syncing.common.SyncingPacketEncoder
import com.srilakshmikanthanp.clipbirdroid.store.Storage
import com.srilakshmikanthanp.clipbirdroid.types.aliases.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.types.device.Device
import com.srilakshmikanthanp.clipbirdroid.types.enums.AuthStatus
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandler
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslHandler
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import java.net.InetSocketAddress
import java.security.cert.X509Certificate


/**
 * Client Class for Syncing the Clipboard
 */
class Client(private val context: Context): Browser.BrowserListener, ChannelInboundHandler {
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
  fun interface OnServerListChangeHandler {
    fun onServerListChanged(servers: List<Device>)
  }

  // Interface for On Server Found
  fun interface OnServerFoundHandler {
    fun onServerFound(server: Device)
  }

  // Interface for On Server Gone
  fun interface OnServerGoneHandler {
    fun onServerGone(server: Device)
  }

  // Interface for On Server state changed
  fun interface OnServerStatusChangeHandler {
    fun onServerStatusChanged(isConnected: Boolean)
  }

  // Interface for On Connection Error
  fun interface OnConnectionErrorHandler {
    fun onConnectionError(error: String)
  }

  // Interface for On Sync Request
  fun interface OnSyncRequestHandler {
    fun onSyncRequest(items: List<Pair<String, ByteArray>>)
  }

  // SSL Verifier Secured
  inner class SSLVerifierSecured: ChannelInboundHandlerAdapter() {
    override fun channelActive(ctx: ChannelHandlerContext) {
      // get the Handler for SSL from Pipeline
      val ssl = ctx.channel().pipeline().get("ssl") as SslHandler

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
    }
  }

  // SSL verifier
  inner class SSLVerifier : ChannelInboundHandlerAdapter() {
    override fun channelActive(ctx: ChannelHandlerContext) {
      // get the Handler for SSL from Pipeline
      val ssl = ctx.channel().pipeline().get("ssl") as SslHandler

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
    }
  }

  // Channel Initializer Secured
  inner class InitializerSecured: ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
      // create SSL Context from cert and private key
      val sslContext = SslContextBuilder.forServer(sslConfig?.first, sslConfig?.second)
        .trustManager(ClipbirdTrustManager()).build()

      // Preprocessing Handlers
      ch.pipeline().addLast("ssl", sslContext?.newHandler(ch.alloc()))
      ch.pipeline().addLast(SSLVerifierSecured())
      ch.pipeline().addLast(AuthenticationEncoder())
      ch.pipeline().addLast(InvalidPacketEncoder())
      ch.pipeline().addLast(SyncingPacketEncoder())
      ch.pipeline().addLast(PacketDecoder())

      // Add the Server Handler
      ch.pipeline().addLast(this@Client)
    }
  }

  // Channel Initializer
  inner class Initializer: ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
      // create SSL Context from cert and private key
      val sslContext = SslContextBuilder.forServer(sslConfig?.first, sslConfig?.second)
        .trustManager(ClipbirdTrustManager()).build()

      // Preprocessing Handlers
      ch.pipeline().addLast("ssl", sslContext?.newHandler(ch.alloc()))
      ch.pipeline().addLast(SSLVerifier())
      ch.pipeline().addLast(AuthenticationEncoder())
      ch.pipeline().addLast(InvalidPacketEncoder())
      ch.pipeline().addLast(SyncingPacketEncoder())
      ch.pipeline().addLast(PacketDecoder())

      // Add the Server Handler
      ch.pipeline().addLast(this@Client)
    }
  }

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
  private fun notifyServerStatusChanged(isConnected: Boolean) {
    for (handler in onServerStatusChangeHandlers) {
      handler.onServerStatusChanged(isConnected)
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
    // create a bootstrap for channel
    val future = Bootstrap().group(NioEventLoopGroup())
      .channel(NioSocketChannel::class.java)
      .handler(InitializerSecured())
      .connect(server.ip, server.port)

    // Add Listener for connection
    future.addListener {
      // If connection Failed
      if (!future.isSuccess) {
        return@addListener notifyConnectionError(future.cause().message!!)
      }

      // if succeed
      channel = future.channel()

      // notify listeners
      notifyServerStatusChanged(true)
    }
  }

  /**
   * Connect to server
   */
  fun connectToServer(server: Device) {
    // create a bootstrap for channel
    val future = Bootstrap().group(NioEventLoopGroup())
      .channel(NioSocketChannel::class.java)
      .handler(Initializer())
      .connect(server.ip, server.port)

    // Add Listener for connection
    future.addListener {
      // If connection Failed
      if (!future.isSuccess) {
        return@addListener notifyConnectionError(future.cause().message!!)
      }

      // if succeed
      channel = future.channel()

      // notify listeners
      notifyServerStatusChanged(true)
    }
  }

  /**
   * Get the Connected Server
   */
  fun getConnectedServer(): Device? {
    if (!this.isConnected()) throw RuntimeException("Not Connected to server")

    val addr = channel!!.remoteAddress() as InetSocketAddress
    val ssl = channel!!.pipeline().get("ssl") as SslHandler
    val cert = ssl.engine().session.peerCertificates[0] as X509Certificate
    val x500Name = JcaX509CertificateHolder(cert).subject
    val cn = x500Name.getRDNs(BCStyle.CN)[0]
    val name = IETFUtils.valueToString(cn.first.value)

    return Device(addr.address, addr.port, name)
  }

  /**
   * Disconnect from the server
   */
  fun disconnectFromServer() {
    if (!this.isConnected()) {
      throw RuntimeException("Not Connected to server")
    } else {
      channel!!.close().also { channel = null }
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
    notifyServerStatusChanged(false)
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
