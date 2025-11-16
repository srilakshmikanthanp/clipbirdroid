package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedServers
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.syncing.ClientServer
import com.srilakshmikanthanp.clipbirdroid.syncing.ClientServerSessionEventListener
import kotlinx.coroutines.CoroutineScope

class BtClientServer(
  private val btResolvedDevice: BtResolvedDevice,
  private val sslConfig: SSLConfig,
  private val trustedServers: TrustedServers,
  private val context: Context,
  private val coroutineScope: CoroutineScope
) : ClientServer(btResolvedDevice.name) {
  override suspend fun connect(listener: ClientServerSessionEventListener) {
    BtClientServerSession(btResolvedDevice, sslConfig, trustedServers, context, listener, coroutineScope).connect()
  }
}
