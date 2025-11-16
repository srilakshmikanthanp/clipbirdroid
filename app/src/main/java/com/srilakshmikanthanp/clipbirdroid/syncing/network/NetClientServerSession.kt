package com.srilakshmikanthanp.clipbirdroid.syncing.network

import com.srilakshmikanthanp.clipbirdroid.common.extensions.awaitSuspend
import com.srilakshmikanthanp.clipbirdroid.common.trust.ClipbirdAllTrustManager
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedServers
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.constants.appMaxIdleReadTime
import com.srilakshmikanthanp.clipbirdroid.constants.appMaxIdleWriteTime
import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket
import com.srilakshmikanthanp.clipbirdroid.packets.PingPongPacket
import com.srilakshmikanthanp.clipbirdroid.packets.PingPongType
import com.srilakshmikanthanp.clipbirdroid.syncing.ClientServerSessionEventListener
import com.srilakshmikanthanp.clipbirdroid.syncing.Session
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
import io.netty.handler.ssl.SslHandshakeCompletionEvent
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import io.netty.handler.timeout.IdleStateHandler
import io.netty.util.AttributeKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import java.security.cert.X509Certificate

class NetClientServerSession(
  private val netResolvedDevice: NetResolvedDevice,
  private val sslConfig: SSLConfig,
  private val trustedServers: TrustedServers,
  private val listener: ClientServerSessionEventListener,
  private val coroutineScope: CoroutineScope
): Session(netResolvedDevice.name), ChannelInboundHandler {
  inner class Initializer : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
      val sslContext = SslContextBuilder.forClient()
        .keyManager(sslConfig.privateKey, sslConfig.certificate)
        .trustManager(ClipbirdAllTrustManager()).build()

      val (r, w) = Pair(appMaxIdleReadTime(), appMaxIdleWriteTime())

      ch.pipeline().addLast(IdleStateHandler(r, w, 0))
      ch.pipeline().addLast(sslContext.newHandler(ch.alloc()))
      ch.pipeline().addLast(SSLVerifier())
      ch.pipeline().addLast(NetworkPacketEncoder())
      ch.pipeline().addLast(PacketDecoder())
      ch.pipeline().addLast(this@NetClientServerSession)
    }
  }

  inner class SSLVerifier : ChannelInboundHandlerAdapter() {
    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
      if (evt !is SslHandshakeCompletionEvent) {
        return super.userEventTriggered(ctx, evt)
      }

      if (!evt.isSuccess) ctx.close().also { return }
      val ssl = ctx.channel().pipeline().get(SslHandler::class.java) as SslHandler
      if (ssl.engine().session.peerCertificates.isEmpty()) ctx.close().also { return }
      val cert = ssl.engine().session.peerCertificates[0] as X509Certificate
      val x500Name = JcaX509CertificateHolder(cert).subject
      val rdns = x500Name.getRDNs(BCStyle.CN)
      if (rdns.isEmpty()) ctx.close().also { return }
      ctx.fireUserEventTriggered(evt)
    }
  }

  private val deviceName = AttributeKey.valueOf<String>("DEVICE_NAME")
  private var channel: Channel? = null

  init {
    coroutineScope.launch {
      trustedServers.trustedServers.collect {
        if (channel != null) {
          _isTrusted.value = trustedServers.isTrustedServer(name, getCertificate())
        }
      }
    }
  }

  private fun onSSLHandShakeComplete(ctx: ChannelHandlerContext, evt: SslHandshakeCompletionEvent) {
    if (!evt.isSuccess) ctx.close().also { return }
    val ssl = ctx.channel().pipeline().get(SslHandler::class.java) as SslHandler
    if (ssl.engine().session.peerCertificates.isEmpty()) ctx.close().also { return }
    val peerCert = ssl.engine().session.peerCertificates[0] as X509Certificate
    val x500Name = JcaX509CertificateHolder(peerCert).subject
    val rdns = x500Name.getRDNs(BCStyle.CN)
    if (rdns.isEmpty()) ctx.close().also { return }
    val name = IETFUtils.valueToString(rdns[0].first.value)
    ctx.channel().attr(deviceName).set(name)
    listener.onConnected(this)
    this._isTrusted.value = trustedServers.isTrustedServer(name, peerCert)
  }

  private fun onIdleStateEvent(ctx: ChannelHandlerContext, evt: IdleStateEvent) {
    if (evt.state() == IdleState.WRITER_IDLE) {
      ctx.writeAndFlush(PingPongPacket(PingPongType.Ping))
    }

    if (evt.state() == IdleState.READER_IDLE) {
      ctx.close()
    }
  }

  override suspend fun sendPacket(packet: NetworkPacket) {
    channel?.writeAndFlush(packet)?.awaitSuspend()
  }

  override suspend fun disconnect() {
    channel?.close()?.awaitSuspend()
    channel = null
  }

  private val _isTrusted = MutableStateFlow(false)
  override val isTrusted = _isTrusted.asStateFlow()

  override fun getCertificate(): X509Certificate {
    val channel = this.channel ?: throw IllegalStateException("Channel is not connected")
    val ssl = channel.pipeline().get(SslHandler::class.java) as SslHandler
    return ssl.engine().session.peerCertificates[0] as X509Certificate
  }

  override fun channelRegistered(ctx: ChannelHandlerContext?) {}

  override fun channelUnregistered(ctx: ChannelHandlerContext?) {}

  override fun channelActive(ctx: ChannelHandlerContext?) {}

  override fun channelReadComplete(ctx: ChannelHandlerContext?) {}

  override fun channelWritabilityChanged(ctx: ChannelHandlerContext?) {}

  override fun handlerAdded(ctx: ChannelHandlerContext?) {}

  override fun handlerRemoved(ctx: ChannelHandlerContext?) {}

  override fun channelInactive(ctx: ChannelHandlerContext?) {
    listener.onDisconnected(this)
  }

  override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any?) {
    if (evt is SslHandshakeCompletionEvent) {
      this.onSSLHandShakeComplete(ctx, evt)
    }

    if (evt is IdleStateEvent) {
      this.onIdleStateEvent(ctx, evt)
    }
  }

  override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    when (msg) {
      is NetworkPacket -> listener.onNetworkPacket(this, msg)
    }
  }

  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    listener.onError(this, cause).also { ctx.close() }
  }

  suspend fun connect() {
    channel = Bootstrap().group(NioEventLoopGroup())
      .channel(NioSocketChannel::class.java)
      .handler(Initializer())
      .connect(netResolvedDevice.address, netResolvedDevice.port)
      .awaitSuspend()
  }
}
