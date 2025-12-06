package com.srilakshmikanthanp.clipbirdroid.syncing.network

import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.common.exceptions.ErrorCodeException
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedServers
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.syncing.ClientServerBrowser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

class NetClientServerBrowser @Inject constructor(
  @ApplicationContext context: Context,
  sslConfig: SSLConfig,
  private val trustedServers: TrustedServers,
  parentScope: CoroutineScope
) : NetBrowserListener, ClientServerBrowser(context, sslConfig) {
  private val coroutineScope = CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]))
  private val clientServers: MutableMap<NetResolvedDevice, NetClientServer> = mutableMapOf()
  private val mdnsBrowser = MdnsNetBrowser(context)

  init {
    mdnsBrowser.addListener(this)
  }

  override fun onServiceRemoved(device: NetResolvedDevice) {
    val removed = clientServers.remove(device) ?: return
    for (l in super.clientEventListeners) l.onServerGone(removed)
  }

  override fun onServiceAdded(device: NetResolvedDevice) {
    val clientServer = NetClientServer(device, sslConfig, trustedServers, coroutineScope)
    clientServers[device] = clientServer
    for (l in super.clientEventListeners) l.onServerFound(clientServer)
  }

  override fun onBrowsingStopped() {
    for (l in super.clientEventListeners) l.onBrowsingStopped()
  }

  override fun onBrowsingStarted() {
    for (l in super.clientEventListeners) l.onBrowsingStarted()
  }

  override fun onStartBrowsingFailed(errorCode: Int) {
    for (l in super.clientEventListeners) l.onBrowsingStartFailed(ErrorCodeException(errorCode))
  }

  override fun onStopBrowsingFailed(errorCode: Int) {
    for (l in super.clientEventListeners) l.onBrowsingStartFailed(ErrorCodeException(errorCode))
  }

  override suspend fun start() {
    mdnsBrowser.start()
  }

  override suspend fun stop() {
    mdnsBrowser.stop()
  }
}

