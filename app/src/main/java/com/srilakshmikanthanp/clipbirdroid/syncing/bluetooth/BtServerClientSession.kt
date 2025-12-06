package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedClients
import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket
import com.srilakshmikanthanp.clipbirdroid.syncing.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.security.cert.X509Certificate

class BtServerClientSession(
  name: String,
  private val certificate: X509Certificate,
  val btConnection: BtConnection,
  private val trustedClients: TrustedClients,
  parentScope: CoroutineScope
): Session(name) {
  private val coroutineScope = CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]))

  override suspend fun sendPacket(packet: NetworkPacket) {
    btConnection.sendPacket(packet)
  }

  override suspend fun disconnect() {
    btConnection.stop()
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
