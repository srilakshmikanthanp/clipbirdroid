package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedClient
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedClients
import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket
import com.srilakshmikanthanp.clipbirdroid.syncing.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import java.security.cert.X509Certificate

class BtServerClientSession(
  name: String,
  private val certificate: X509Certificate,
  val btSession: BtSession,
  private val trustedClients: TrustedClients,
  parentScope: CoroutineScope
): Session(name) {
  private val coroutineScope = CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]))

  override suspend fun sendPacket(packet: NetworkPacket) {
    btSession.sendPacket(packet)
  }

  override suspend fun disconnect() {
    btSession.stop()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  override val isTrusted = trustedClients.trustedClients.mapLatest {
    trustedClients.isTrustedClient(TrustedClient(name, getCertificate()))
  }.stateIn(
    initialValue = false,
    scope = coroutineScope,
    started = SharingStarted.Eagerly,
  )

  override fun getCertificate(): X509Certificate {
    return certificate
  }
}
