package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedServers
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket
import com.srilakshmikanthanp.clipbirdroid.syncing.ClientServerSessionEventListener
import com.srilakshmikanthanp.clipbirdroid.syncing.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.cert.X509Certificate

@SuppressLint("MissingPermission")
class BtClientServerSession(
  private val btResolvedDevice: BtResolvedDevice,
  private val sslConfig: SSLConfig,
  private val trustedServers: TrustedServers,
  private val context: Context,
  private val listener: ClientServerSessionEventListener,
  parentScope: CoroutineScope
): Session(btResolvedDevice.name), BtConnectionListener {
  private val coroutineScope = CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]))
  private val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?)?.adapter
  private var btSession: BtSession? = null

  init {
    coroutineScope.launch {
      trustedServers.trustedServers.collect {
        if (btSession?.isHandshakeCompleted() == true) {
          _isTrusted.value = trustedServers.isTrustedServer(name, getCertificate())
        }
      }
    }
  }

  private suspend fun connect(socket: BluetoothSocket) = withContext(Dispatchers.IO) {
    try {
      socket.connect()
      this@BtClientServerSession.btSession = BtSession(this@BtClientServerSession, coroutineScope, socket, sslConfig)
      this@BtClientServerSession.btSession!!.start()
    } catch (e: Exception) {
      listener.onError(this@BtClientServerSession, e)
    }
  }

  override suspend fun sendPacket(packet: NetworkPacket) {
    btSession?.sendPacket(packet)
  }

  override suspend fun disconnect() {
    btSession?.stop()
    btSession = null
  }

  private val _isTrusted = MutableStateFlow(false)
  override val isTrusted = _isTrusted.asStateFlow()

  override fun getCertificate(): X509Certificate {
    return btSession!!.getPeerCertificate()
  }

  suspend fun connect() = withContext(Dispatchers.IO) {
    val btAdapter = requireNotNull(bluetoothAdapter)
    val remoteDevice = btAdapter.getRemoteDevice(btResolvedDevice.address)
    val socket = remoteDevice.createRfcommSocketToServiceRecord(BtConstants.serviceUuid)
    btAdapter.cancelDiscovery()
    this@BtClientServerSession.connect(socket)
  }

  override fun onHandShakeCompleted(btSession: BtSession) {
    _isTrusted.value = trustedServers.isTrustedServer(name, getCertificate())
    listener.onConnected(this)
  }

  override fun onDisconnected(btSession: BtSession) {
    listener.onDisconnected(this)
  }

  override fun onError(btSession: BtSession, cause: Throwable) {
    listener.onError(this, cause)
  }

  override fun onNetworkPacket(btSession: BtSession, packet: NetworkPacket) {
    listener.onNetworkPacket(this, packet)
  }
}
