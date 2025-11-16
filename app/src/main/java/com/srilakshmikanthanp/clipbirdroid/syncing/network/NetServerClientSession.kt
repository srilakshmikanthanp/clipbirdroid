package com.srilakshmikanthanp.clipbirdroid.syncing.network

import com.srilakshmikanthanp.clipbirdroid.common.extensions.awaitSuspend
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedClients
import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket
import com.srilakshmikanthanp.clipbirdroid.syncing.Session
import io.netty.channel.ChannelHandlerContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.net.InetAddress
import java.security.cert.X509Certificate

class NetServerClientSession(
  name: String,
  private val certificate: X509Certificate,
  val trustedClients: TrustedClients,
  val context: ChannelHandlerContext,
  private val coroutineScope: CoroutineScope
): Session(name) {
  override suspend fun sendPacket(packet: NetworkPacket) {
    context.writeAndFlush(packet).awaitSuspend()
  }

  override suspend fun disconnect() {
    context.close().awaitSuspend()
  }

  override val isTrusted = trustedClients.trustedClients.map {
    trustedClients.isTrustedClient(name, getCertificate())
  }.stateIn(
    initialValue = trustedClients.isTrustedClient(name, getCertificate()),
    scope = coroutineScope,
    started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
  )

  override fun getCertificate(): X509Certificate {
    return certificate
  }
}
