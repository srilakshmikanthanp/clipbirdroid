package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedServers
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.syncing.ClientServer
import com.srilakshmikanthanp.clipbirdroid.syncing.ClientServerSessionEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

class BtClientServer(
  private val btResolvedDevice: BtResolvedDevice,
  private val sslConfig: SSLConfig,
  private val trustedServers: TrustedServers,
  private val context: Context,
  parentScope: CoroutineScope
) : ClientServer(btResolvedDevice.name) {
  private val coroutineScope = CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]))

  override suspend fun connect(listener: ClientServerSessionEventListener) {
    BtClientServerSession(btResolvedDevice, sslConfig, trustedServers, context, listener, coroutineScope).connect()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BtClientServer) return false
    return btResolvedDevice == other.btResolvedDevice
  }

  override fun hashCode(): Int {
    return btResolvedDevice.hashCode()
  }
}
