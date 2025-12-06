package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.content.Context
import android.util.Log
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedClients
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.constants.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket
import com.srilakshmikanthanp.clipbirdroid.syncing.Server
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import javax.inject.Inject

@SuppressLint("MissingPermission")
class BtServer @Inject constructor(
  @ApplicationContext context: Context,
  sslConfig: SSLConfig,
  private val trustedClients: TrustedClients,
  parentScope: CoroutineScope
): Server(context, sslConfig), BtConnectionListener {
  private val coroutineScope = CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]))
  private val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?)?.adapter
  private val clients = mutableMapOf<String, BtConnection>()
  private var acceptJob: Job? = null
  private var serverSocket: BluetoothServerSocket? = null

  companion object {
    const val TAG = "BtServer"
  }

  private suspend fun acceptConnections() = withContext(Dispatchers.IO) {
    val serverSocket = this@BtServer.serverSocket ?: return@withContext
    while (this@BtServer.acceptJob?.isActive == true) {
      try {
        val connection = BtConnection(this@BtServer, coroutineScope, serverSocket.accept(), sslConfig)
        clients[connection.getRemoteDeviceName()] = connection
        connection.start()
      } catch (e: IOException) {
        Log.e(TAG, e.message, e)
      }
    }
  }

  override suspend fun start() {
    if (this.serverSocket != null) throw IllegalStateException("Server is already running")
    this.serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(appMdnsServiceName(context), BtConstants.serviceUuid)
    acceptJob = coroutineScope.launch { acceptConnections() }
  }

  override suspend fun stop() {
    serverSocket?.close()
    serverSocket = null
    acceptJob?.cancelAndJoin()
    acceptJob = null
    clients.values.forEach { it.stop() }
    clients.clear()
  }

  override fun onHandShakeCompleted(btConnection: BtConnection) {
    val certificate = btConnection.getPeerCertificate()
    val session = BtServerClientSession(btConnection.getRemoteDeviceName(), certificate, btConnection, trustedClients, coroutineScope)
    btConnection.setAttribute(BtServerClientSession::class.simpleName!!, session)
    super.serverEventListeners.forEach { it.onClientConnected(session) }
  }

  override fun onDisconnected(btConnection: BtConnection) {
    this.clients.remove(btConnection.getRemoteDeviceName())
    val session = btConnection.getAttribute(BtServerClientSession::class.simpleName!!) as BtServerClientSession? ?: return
    super.serverEventListeners.forEach { it.onClientDisconnected(session) }
  }

  override fun onError(btConnection: BtConnection, cause: Throwable) {
    val session = btConnection.getAttribute(BtServerClientSession::class.simpleName!!) as BtServerClientSession? ?: return
    super.serverEventListeners.forEach { it.onClientError(session, cause) }
  }

  override fun onNetworkPacket(btConnection: BtConnection, packet: NetworkPacket) {
    val session = btConnection.getAttribute(BtServerClientSession::class.simpleName!!) as BtServerClientSession? ?: return
    super.serverEventListeners.forEach { it.onNetworkPacket(session, packet) }
  }
}
