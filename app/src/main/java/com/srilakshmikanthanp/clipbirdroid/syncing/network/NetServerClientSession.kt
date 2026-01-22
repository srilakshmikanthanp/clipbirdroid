package com.srilakshmikanthanp.clipbirdroid.syncing.network

import com.srilakshmikanthanp.clipbirdroid.common.extensions.awaitSuspend
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedClient
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedClients
import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket
import com.srilakshmikanthanp.clipbirdroid.syncing.Session
import io.netty.channel.ChannelHandlerContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import java.security.cert.X509Certificate


class NetServerClientSession(
  name: String,
  private val certificate: X509Certificate,
  val trustedClients: TrustedClients,
  val context: ChannelHandlerContext,
  parentScope: CoroutineScope
): Session(name) {
  private val coroutineScope = CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]))

  override suspend fun sendPacket(packet: NetworkPacket) {
    context.writeAndFlush(packet).awaitSuspend()
  }

  override suspend fun disconnect() {
    context.close().awaitSuspend()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  override val isTrusted = trustedClients.trustedClients.mapLatest {
    trustedClients.isTrustedClient(TrustedClient(name, getCertificate()))
  }.stateIn(
    scope = coroutineScope,
    started = SharingStarted.Eagerly,
    initialValue = false
  )

  override fun getCertificate(): X509Certificate {
    return certificate
  }
}
