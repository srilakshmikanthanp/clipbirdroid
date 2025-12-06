package com.srilakshmikanthanp.clipbirdroid.syncing.network

import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.common.exceptions.ErrorCodeException
import com.srilakshmikanthanp.clipbirdroid.common.extensions.awaitSuspend
import com.srilakshmikanthanp.clipbirdroid.common.trust.ClipbirdAllTrustManager
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedClients
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.constants.appMaxIdleReadTime
import com.srilakshmikanthanp.clipbirdroid.constants.appMaxIdleWriteTime
import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket
import com.srilakshmikanthanp.clipbirdroid.packets.PingPongPacket
import com.srilakshmikanthanp.clipbirdroid.packets.PingPongType
import com.srilakshmikanthanp.clipbirdroid.syncing.Server
import dagger.hilt.android.qualifiers.ApplicationContext
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import java.net.InetSocketAddress
import java.security.cert.X509Certificate
import javax.inject.Inject
import kotlin.io.path.fileVisitor

class NetServer @Inject constructor(
  @ApplicationContext context: Context,
  sslConfig: SSLConfig,
  private val trustedClients: TrustedClients,
  parentScope: CoroutineScope
) : NetRegisterListener, Server(context, sslConfig), ChannelInboundHandler {
  private val coroutineScope = CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]))

  inner class NewChannelInitializer : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
      val sslContext = SslContextBuilder.forServer(sslConfig.privateKey, sslConfig.certificate)
        .trustManager(ClipbirdAllTrustManager())
        .clientAuth(ClientAuth.REQUIRE)
        .build()
      val (r, w) = Pair(appMaxIdleReadTime(), appMaxIdleWriteTime())

      ch.pipeline().addLast(IdleStateHandler(r, w, 0))
      ch.pipeline().addLast(sslContext?.newHandler(ch.alloc()))
      ch.pipeline().addLast(NetworkPacketEncoder())
      ch.pipeline().addLast(PacketDecoder())
      ch.pipeline().addLast(this@NetServer)
    }
  }

  private val deviceName = AttributeKey.valueOf<String>("DEVICE_NAME")
  private val sessions: MutableMap<String, NetServerClientSession> = mutableMapOf()
  private var sslServer: Channel? = null
  private val mdnsRegister = MdnsNetRegister(context)

  private fun onSSLHandShakeComplete(ctx: ChannelHandlerContext, evt: SslHandshakeCompletionEvent) {
    if (!evt.isSuccess) ctx.close().also { return }
    val ssl = ctx.channel().pipeline().get(SslHandler::class.java) as SslHandler
    if(ssl.engine().session.peerCertificates.isEmpty()) ctx.close().also { return }
    val peerCert = ssl.engine().session.peerCertificates[0] as X509Certificate
    val x500Name = JcaX509CertificateHolder(peerCert).subject
    val rdns = x500Name.getRDNs(BCStyle.CN)
    if (rdns.isEmpty()) ctx.close().also { return }
    val name = IETFUtils.valueToString(rdns[0].first.value)
    ctx.channel().attr(deviceName).set(name)
    sessions[name] = NetServerClientSession(name, peerCert, trustedClients, ctx, coroutineScope)
    super.serverEventListeners.forEach { it.onClientConnected(sessions[name]!!) }
  }

  private fun onIdleStateEvent(ctx: ChannelHandlerContext, evt: IdleStateEvent) {
    if (evt.state() == IdleState.WRITER_IDLE) ctx.writeAndFlush(PingPongPacket(PingPongType.Ping))
    if (evt.state() == IdleState.READER_IDLE) ctx.close()
  }

  init {
    mdnsRegister.addRegisterListener(this)
  }

  override fun channelWritabilityChanged(ctx: ChannelHandlerContext?) {}

  override fun channelRegistered(ctx: ChannelHandlerContext?) {}

  override fun handlerAdded(ctx: ChannelHandlerContext?) {}

  override fun handlerRemoved(ctx: ChannelHandlerContext?) {}

  override fun channelUnregistered(ctx: ChannelHandlerContext?) {}

  override fun channelActive(ctx: ChannelHandlerContext) {}

  override fun channelReadComplete(ctx: ChannelHandlerContext?) {}

  override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any?) {
    if (evt is SslHandshakeCompletionEvent) this.onSSLHandShakeComplete(ctx, evt)
    if (evt is IdleStateEvent) this.onIdleStateEvent(ctx, evt)
  }

  override fun channelInactive(ctx: ChannelHandlerContext) {
    super.serverEventListeners.forEach { fileVisitor { it.onClientDisconnected(sessions[ctx.channel().attr(deviceName).get()]!!) } }
  }

  override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    when (msg) {
      is NetworkPacket -> super.serverEventListeners.forEach { it.onNetworkPacket(sessions[ctx.channel().attr(deviceName).get()]!!, msg) }
    }
  }

  @Deprecated("Deprecated in Java")
  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    serverEventListeners.forEach { it.onClientError(sessions[ctx.channel().attr(deviceName).get()]!!, cause) }.also { ctx.close() }
  }

  override fun onServiceUnregistered() {
    super.serverEventListeners.forEach { it.onServiceUnregistered() }
  }

  override fun onServiceRegistered() {
    super.serverEventListeners.forEach { it.onServiceRegistered() }
  }

  override fun onServiceRegistrationFailed(errorCode: Int) {
    super.serverEventListeners.forEach { it.onServiceRegistrationFailed(ErrorCodeException(errorCode)) }
  }

  override fun onServiceUnregistrationFailed(errorCode: Int) {
    super.serverEventListeners.forEach { it.onServiceUnregistrationFailed(ErrorCodeException(errorCode)) }
  }

  override suspend fun start() {
    val workerGroup = NioEventLoopGroup()
    val bossGroup = NioEventLoopGroup()
    val server = ServerBootstrap()
     .channel(NioServerSocketChannel::class.java)
     .group(bossGroup, workerGroup)
     .childHandler(NewChannelInitializer())
     .childOption(ChannelOption.SO_KEEPALIVE, true)
     .bind(0)
     .awaitSuspend()

    val address = server.localAddress() as InetSocketAddress
    mdnsRegister.registerService(address.port)
    this.sslServer = server
  }

  override suspend fun stop() {
    mdnsRegister.unregisterService()
    this.sessions.values.forEach { it.context.close().awaitSuspend() }
    if (!this.sslServer?.isOpen!!) return
    sslServer?.close()?.awaitSuspend()
    this.sslServer = null
  }
}
