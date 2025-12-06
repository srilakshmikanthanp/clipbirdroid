package com.srilakshmikanthanp.clipbirdroid.syncing.manager

import com.srilakshmikanthanp.clipbirdroid.clipboard.ClipboardContent
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedClients
import com.srilakshmikanthanp.clipbirdroid.packets.AuthenticationPacket
import com.srilakshmikanthanp.clipbirdroid.packets.CertificateExchangePacket
import com.srilakshmikanthanp.clipbirdroid.packets.InvalidRequestPacket
import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket
import com.srilakshmikanthanp.clipbirdroid.packets.PingPongPacket
import com.srilakshmikanthanp.clipbirdroid.packets.PingPongType
import com.srilakshmikanthanp.clipbirdroid.packets.SyncingItem
import com.srilakshmikanthanp.clipbirdroid.packets.SyncingPacket
import com.srilakshmikanthanp.clipbirdroid.syncing.Server
import com.srilakshmikanthanp.clipbirdroid.syncing.ServerEventListener
import com.srilakshmikanthanp.clipbirdroid.syncing.Session
import com.srilakshmikanthanp.clipbirdroid.syncing.SyncRequestHandler
import com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth.BtServer
import com.srilakshmikanthanp.clipbirdroid.syncing.network.NetServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerManager @Inject constructor(
  private val netServer: NetServer,
  private val btServer: BtServer,
  parentScope: CoroutineScope,
  private val trustedClients: TrustedClients
) : HostManager, ServerEventListener {
  private val coroutineScope = CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]))
  private val serverManagerEventListeners: MutableList<ServerManagerEventListener> = mutableListOf()
  private val syncRequestHandlers = mutableListOf<SyncRequestHandler>()
  private val clients: MutableList<Session> = mutableListOf()
  private var server: Server? = null

  private fun onSyncingPacket(session: Session, packet: SyncingPacket) {
    if (!session.isTrusted.value) return
    val items = packet.getItems().map { ClipboardContent(String(it.getMimeType()), it.getPayload()) }
    syncRequestHandlers.forEach { it.onSyncRequest(items) }
  }

  private fun onPingPongPacket(session: Session, packet: PingPongPacket) {
    if (packet.getPingType() == PingPongType.Ping) {
      coroutineScope.launch { session.sendPacket(PingPongPacket(PingPongType.Pong)) }
    }
  }

  override fun addSyncRequestHandler(handler: SyncRequestHandler) {
    syncRequestHandlers.add(handler)
  }

  override fun removeSyncRequestHandler(handler: SyncRequestHandler) {
    syncRequestHandlers.remove(handler)
  }

  fun addServerManagerEventListener(listener: ServerManagerEventListener) {
    serverManagerEventListeners.add(listener)
  }

  fun removeServerManagerEventListener(listener: ServerManagerEventListener) {
    serverManagerEventListeners.remove(listener)
  }

  override suspend fun synchronize(items: List<ClipboardContent>) {
    val syncingPacket = SyncingPacket(items.map { SyncingItem(it.mimeType.toByteArray(), it.data) }.toTypedArray())
    coroutineScope.launch { clients.forEach { if(it.isTrusted.value) it.sendPacket(syncingPacket) } }
  }

  override fun onClientDisconnected(client: Session) {
    clients.remove(client).also { serverManagerEventListeners.forEach { it.onClientDisConnected(client) } }
  }

  override fun onClientConnected(client: Session) {
    clients.add(client).also { serverManagerEventListeners.forEach { it.onClientConnected(client) } }
  }

  override fun onServiceUnregistrationFailed(e: Throwable) {
    serverManagerEventListeners.forEach { it.onServiceUnregisteringFailed(e) }
  }

  override fun onClientError(client: Session, e: Throwable) {
    serverManagerEventListeners.forEach { it.onError(client, e) }
  }

  override fun onServiceRegistered() {
    serverManagerEventListeners.forEach { it.onServiceRegistered() }
  }

  override fun onServiceUnregistered() {
    serverManagerEventListeners.forEach { it.onServiceUnregistered() }
  }

  override fun onServiceRegistrationFailed(e: Throwable) {
    serverManagerEventListeners.forEach { it.onServiceRegisteringFailed(e) }
  }

  override fun onNetworkPacket(client: Session, networkPacket: NetworkPacket) {
    when (networkPacket) {
      is PingPongPacket -> onPingPongPacket(client, networkPacket)
      is AuthenticationPacket -> {}
      is SyncingPacket -> onSyncingPacket(client, networkPacket)
      is InvalidRequestPacket -> {}
      is CertificateExchangePacket -> {}
    }
  }

  override suspend fun start(useBluetooth: Boolean) {
    server = if (useBluetooth) btServer else netServer
    server?.addServerEventListener(this)
    server?.start()
  }

  override suspend fun stop() {
    server?.stop()
    server?.removeServerEventListener(this@ServerManager)
    server = null
  }
}
