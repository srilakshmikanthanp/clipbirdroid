package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedServer
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
): Session(btResolvedDevice.name), BtSocketSessionListener {
  private val coroutineScope = CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]))
  private val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?)?.adapter
  private var btSocketSession: BtSocketSession? = null

  private val _isTrusted = MutableStateFlow(false)
  override val isTrusted = _isTrusted.asStateFlow()

  init {
    coroutineScope.launch {
      trustedServers.trustedServers.collect {
        if (btSocketSession?.isHandshakeCompleted() == true) {
          _isTrusted.value = trustedServers.isTrustedServer(TrustedServer(name, getCertificate()))
        }
      }
    }
  }

  suspend fun connect() = withContext(Dispatchers.IO) {
    val btAdapter = requireNotNull(bluetoothAdapter)
    val remoteDevice = btAdapter.getRemoteDevice(btResolvedDevice.address)
    val socket = remoteDevice.createRfcommSocketToServiceRecord(BtConstants.serviceUuid)
    btAdapter.cancelDiscovery()
    socket.connect()
    this@BtClientServerSession.btSocketSession = BtSocketSession(this@BtClientServerSession, coroutineScope, socket, sslConfig)
    this@BtClientServerSession.btSocketSession!!.start()
  }

  override suspend fun sendPacket(packet: NetworkPacket) {
    btSocketSession?.sendPacket(packet)
  }

  override suspend fun disconnect() {
    btSocketSession?.stop()
    btSocketSession = null
  }

  override fun getCertificate(): X509Certificate {
    return btSocketSession!!.getPeerCertificate()
  }

  override fun onHandShakeCompleted(btSocketSession: BtSocketSession) {
    coroutineScope.launch {
      _isTrusted.value = trustedServers.isTrustedServer(TrustedServer(name, getCertificate()))
      listener.onConnected(this@BtClientServerSession)
    }
  }

  override fun onDisconnected(btSocketSession: BtSocketSession) {
    listener.onDisconnected(this)
  }

  override fun onError(btSocketSession: BtSocketSession, cause: Throwable) {
    listener.onError(this, cause)
  }

  override fun onNetworkPacket(btSocketSession: BtSocketSession, packet: NetworkPacket) {
    listener.onNetworkPacket(this, packet)
  }
}
