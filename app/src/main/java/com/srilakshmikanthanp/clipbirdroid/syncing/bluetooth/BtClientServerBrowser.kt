package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedServers
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.syncing.ClientServerBrowser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

class BtClientServerBrowser @Inject constructor(
  @ApplicationContext context: Context,
  sslConfig: SSLConfig,
  private val trustedServers: TrustedServers,
  parentScope: CoroutineScope
) : ClientServerBrowser(context, sslConfig), BtBrowserListener {
  private val coroutineScope = CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]))

  private val clientServers: MutableMap<BtResolvedDevice, BtClientServer> = mutableMapOf()
  private var browser: BtDeviceConnectionBrowser? = null

  override fun onServiceRemoved(device: BtResolvedDevice) {
    super.clientEventListeners.forEach { it.onServerGone(clientServers[device]!!) }
  }

  override fun onServiceAdded(device: BtResolvedDevice) {
    val clientServer = BtClientServer(device, sslConfig, trustedServers, context, coroutineScope)
    clientServers[device] = clientServer
    super.clientEventListeners.forEach { it.onServerFound(clientServer) }
  }

  override fun onBrowsingStopped() {
    super.clientEventListeners.forEach { it.onBrowsingStopped() }
  }

  override fun onBrowsingStarted() {
    super.clientEventListeners.forEach { it.onBrowsingStarted() }
  }

  override suspend fun start() {
    this.browser = BtDeviceConnectionBrowser(context, coroutineScope)
    browser?.addListener(this)
    browser?.start()
  }

  override suspend fun stop() {
    browser?.stop()
  }
}
