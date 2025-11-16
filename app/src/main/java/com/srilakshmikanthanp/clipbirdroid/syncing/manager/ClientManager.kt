package com.srilakshmikanthanp.clipbirdroid.syncing.manager

import com.srilakshmikanthanp.clipbirdroid.clipboard.ClipboardContent
import com.srilakshmikanthanp.clipbirdroid.packets.AuthenticationPacket
import com.srilakshmikanthanp.clipbirdroid.packets.AuthenticationStatus
import com.srilakshmikanthanp.clipbirdroid.packets.CertificateExchangePacket
import com.srilakshmikanthanp.clipbirdroid.packets.InvalidRequestPacket
import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket
import com.srilakshmikanthanp.clipbirdroid.packets.PingPongPacket
import com.srilakshmikanthanp.clipbirdroid.packets.PingPongType
import com.srilakshmikanthanp.clipbirdroid.packets.SyncingItem
import com.srilakshmikanthanp.clipbirdroid.packets.SyncingPacket
import com.srilakshmikanthanp.clipbirdroid.syncing.ClientServer
import com.srilakshmikanthanp.clipbirdroid.syncing.ClientServerBrowser
import com.srilakshmikanthanp.clipbirdroid.syncing.ClientServerBrowserEventListener
import com.srilakshmikanthanp.clipbirdroid.syncing.ClientServerSessionEventListener
import com.srilakshmikanthanp.clipbirdroid.syncing.Session
import com.srilakshmikanthanp.clipbirdroid.syncing.SyncRequestHandler
import com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth.BtClientServerBrowser
import com.srilakshmikanthanp.clipbirdroid.syncing.network.NetClientServerBrowser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientManager @Inject constructor(
  private val netClientServerBrowser: NetClientServerBrowser,
  private val btClientServerBrowser: BtClientServerBrowser,
  private val coroutineScope: CoroutineScope
) : HostManager, ClientServerBrowserEventListener, ClientServerSessionEventListener {
  private val clientManagerEventListeners = mutableListOf<ClientManagerEventListener>()
  private val syncRequestHandlers = mutableListOf<SyncRequestHandler>()
  private var clientServerBrowser: ClientServerBrowser? = null
  private var session: Session? = null

  private fun onAuthenticationPacket(session: Session, packet: AuthenticationPacket) {
    if (packet.getAuthStatus() == AuthenticationStatus.AuthOkay) {
      clientManagerEventListeners.forEach { it.onConnected(session) }
    } else {
      coroutineScope.launch { session.disconnect() }
    }
  }

  private fun onInvalidRequestPacket(session: Session, packet: InvalidRequestPacket) {
    this.clientManagerEventListeners.forEach { it.onError(session, RuntimeException(packet.getErrorMessage().toString())) }
  }

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

  fun addClientManagerEventListener(listener: ClientManagerEventListener) {
    clientManagerEventListeners.add(listener)
  }

  fun removeClientManagerEventListener(listener: ClientManagerEventListener) {
    clientManagerEventListeners.remove(listener)
  }

  override suspend fun synchronize(items: List<ClipboardContent>) {
    if (session == null || !session!!.isTrusted.value) return
    session?.sendPacket(SyncingPacket(items.map { SyncingItem(it.mimeType.toByteArray(), it.data) }.toTypedArray()))
  }

  override fun onServerFound(server: ClientServer) {
    clientManagerEventListeners.forEach { it.onServerFound(server) }
  }

  override fun onServerGone(server: ClientServer) {
    clientManagerEventListeners.forEach { it.onServerGone(server) }
  }

  override fun onBrowsingStarted() {
    clientManagerEventListeners.forEach { it.onBrowsingStarted() }
  }

  override fun onBrowsingStopped() {
    clientManagerEventListeners.forEach { it.onBrowsingStopped() }
  }

  override fun onBrowsingStartFailed(e: Throwable) {
    clientManagerEventListeners.forEach { it.onBrowsingStartFailed(e) }
  }

  override fun onBrowsingStopFailed(e: Throwable) {
    clientManagerEventListeners.forEach { it.onBrowsingStopFailed(e) }
  }

  override fun onConnected(session: Session) {
    this.session = session
  }

  override fun onDisconnected(session: Session) {
    this.session = null.also { clientManagerEventListeners.forEach { it.onDisconnected(session) } }
  }

  override fun onError(session: Session, e: Throwable) {
    clientManagerEventListeners.forEach { it.onError(session, e) }
  }

  override fun onNetworkPacket(session: Session, networkPacket: NetworkPacket) {
    when (networkPacket) {
      is AuthenticationPacket -> onAuthenticationPacket(session, networkPacket)
      is SyncingPacket -> onSyncingPacket(session, networkPacket)
      is InvalidRequestPacket -> onInvalidRequestPacket(session, networkPacket)
      is PingPongPacket -> onPingPongPacket(session, networkPacket)
      is CertificateExchangePacket -> {}
    }
  }

  override suspend fun start(useBluetooth: Boolean) {
    this.clientServerBrowser = if (useBluetooth) btClientServerBrowser else netClientServerBrowser
    clientServerBrowser?.addClientServerBrowserEventListener(this@ClientManager)
    clientServerBrowser?.start()
  }

  override suspend fun stop() {
    clientServerBrowser?.stop()
    clientServerBrowser?.removeClientServerBrowserEventListener(this@ClientManager)
    clientServerBrowser = null
  }
}
