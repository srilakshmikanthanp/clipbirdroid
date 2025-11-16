package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.packets.CertificateExchangePacket
import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket
import com.srilakshmikanthanp.clipbirdroid.packets.toNetworkPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.nio.ByteBuffer
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
@SuppressLint("MissingPermission")
class BtConnection(
  private val listener: BtConnectionListener,
  private val coroutineScope: CoroutineScope,
  private val socket: BluetoothSocket,
  private val sslConfig: SSLConfig,
) {
  private val inputStream = DataInputStream(socket.inputStream)
  private val writeMutex = Mutex()
  private val closed = AtomicBoolean(false)
  private val attributes: MutableMap<String, Any> = mutableMapOf()
  private var certificate: X509Certificate? = null
  private var socketJob: Job? = null

  private suspend fun notifyHandShakeCompleted() {
    withContext(Dispatchers.Main) { listener.onHandShakeCompleted(this@BtConnection) }
  }

  private suspend fun notifyError(e: Exception) {
    withContext(Dispatchers.Main) { listener.onError(this@BtConnection, e) }
  }

  private suspend fun notifyDisconnected() {
    withContext(Dispatchers.Main) { listener.onDisconnected(this@BtConnection) }
  }

  private suspend fun nextPacket(): NetworkPacket = withContext((Dispatchers.IO)) {
    val length = inputStream.readInt()
    val bytes = ByteArray(length - Int.SIZE_BYTES)
    inputStream.readFully(bytes)
    val buffer = ByteBuffer.allocate(length)
    buffer.putInt(length).put(bytes).flip()
    return@withContext buffer.toNetworkPacket()
  }

  private suspend fun handshake() {
    this.sendPacket(CertificateExchangePacket(sslConfig.certificate.encoded))
    val response = this.nextPacket()
    if (response !is CertificateExchangePacket) throw IllegalStateException("Expected CertificateExchangePacket, got ${response::class.java.simpleName}")
    val certFactory = CertificateFactory.getInstance("X.509")
    this.certificate = certFactory.generateCertificate(response.getCertificate().inputStream()) as X509Certificate
    notifyHandShakeCompleted()
  }

  private suspend fun readLoop() {
    while (isConnected()) {
      nextPacket().also { withContext(Dispatchers.Main) { listener.onNetworkPacket(this@BtConnection, it) } }
    }
  }

  fun open() {
    socketJob = coroutineScope.launch(Dispatchers.IO) {
      try {
        closed.store(false)
        handshake()
        readLoop()
      } catch (e: Exception) {
        notifyError(e)
        socketJob = null
        close()
      }
    }
  }

  suspend fun sendPacket(packet: NetworkPacket): Unit = withContext(Dispatchers.IO) {
    try {
      writeMutex.withLock { socket.outputStream.apply { write(packet.toByteArray()) }.apply { flush() } }
    } catch (e: Exception) {
      notifyError(e)
      close()
    }
  }

  suspend fun close() {
    if (!closed.compareAndSet(expectedValue = false, newValue = true)) return
    socket.close()
    socketJob?.cancelAndJoin()
    socketJob = null
    notifyDisconnected()
  }

  fun getPeerCertificate(): X509Certificate {
    return certificate ?: throw IllegalStateException("Handshake is not completed yet")
  }

  fun isConnected(): Boolean {
    return socket.isConnected
  }

  fun isHandshakeCompleted(): Boolean {
    return certificate != null
  }

  fun getRemoteDeviceName(): String {
    return socket.remoteDevice.name ?: socket.remoteDevice.address
  }

  fun setAttribute(key: String, value: Any) {
    attributes[key] = value
  }

  fun getAttribute(key: String): Any? {
    return attributes[key]
  }
}
