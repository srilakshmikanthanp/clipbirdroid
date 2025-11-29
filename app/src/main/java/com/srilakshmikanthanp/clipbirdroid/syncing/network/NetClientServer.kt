package com.srilakshmikanthanp.clipbirdroid.syncing.network

import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedServers
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.syncing.ClientServer
import com.srilakshmikanthanp.clipbirdroid.syncing.ClientServerSessionEventListener
import kotlinx.coroutines.CoroutineScope

class NetClientServer(
  private val netResolvedDevice: NetResolvedDevice,
  private val sslConfig: SSLConfig,
  private val trustedServers: TrustedServers,
  private val coroutineScope: CoroutineScope
) : ClientServer(netResolvedDevice.name) {
  override suspend fun connect(listener: ClientServerSessionEventListener) {
    NetClientServerSession(netResolvedDevice, sslConfig, trustedServers, listener, coroutineScope).connect()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is NetClientServer) return false
    return netResolvedDevice == other.netResolvedDevice
  }

  override fun hashCode(): Int {
    return netResolvedDevice.hashCode()
  }
}
