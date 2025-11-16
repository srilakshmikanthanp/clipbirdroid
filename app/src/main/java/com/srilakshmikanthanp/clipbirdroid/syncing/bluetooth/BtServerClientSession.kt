package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedClients
import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket
import com.srilakshmikanthanp.clipbirdroid.syncing.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.security.cert.X509Certificate

class BtServerClientSession(
  name: String,
  private val certificate: X509Certificate,
  val btConnection: BtConnection,
  private val trustedClients: TrustedClients,
  private val coroutineScope: CoroutineScope
): Session(name) {
  override suspend fun sendPacket(packet: NetworkPacket) {
    btConnection.sendPacket(packet)
  }

  override suspend fun disconnect() {
    btConnection.close()
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
