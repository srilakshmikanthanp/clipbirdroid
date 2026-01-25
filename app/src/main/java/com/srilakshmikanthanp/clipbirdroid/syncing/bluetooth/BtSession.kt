package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.constants.appMaxIdleReadTime
import com.srilakshmikanthanp.clipbirdroid.constants.appMaxIdleWriteTime
import com.srilakshmikanthanp.clipbirdroid.packets.CertificateExchangePacket
import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket
import com.srilakshmikanthanp.clipbirdroid.packets.PingPongPacket
import com.srilakshmikanthanp.clipbirdroid.packets.PingPongType
import com.srilakshmikanthanp.clipbirdroid.packets.toNetworkPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
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
class BtSession(
  private val listener: BtSessionListener,
  parentScope: CoroutineScope,
  private val socket: BluetoothSocket,
  private val sslConfig: SSLConfig,
) {
  private val coroutineScope = CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]))
  private val dataInputStream = DataInputStream(socket.inputStream)
  private val writeMutex = Mutex()
  private val closed = AtomicBoolean(false)
  private val attributes: MutableMap<String, Any> = mutableMapOf()
  private var certificate: X509Certificate? = null
  private var socketJob: Job? = null
  private var watchJob: Job? = null
  private var lastReadTime: Long = 0
  private var lastWriteTime: Long = 0

  private suspend fun notifyHandShakeCompleted() {
    withContext(Dispatchers.Main) { listener.onHandShakeCompleted(this@BtSession) }
  }

  private suspend fun notifyError(e: Exception) {
    withContext(Dispatchers.Main) { listener.onError(this@BtSession, e) }
  }

  private suspend fun notifyDisconnected() {
    withContext(Dispatchers.Main) { listener.onDisconnected(this@BtSession) }
  }

  private suspend fun watch() = withContext(Dispatchers.IO) {
    while (isConnected()) {
      val currentTime = System.currentTimeMillis()
      if (currentTime - lastReadTime > appMaxIdleReadTime()) {
        this@BtSession.sendPacket(PingPongPacket(PingPongType.Ping))
      }
      if (currentTime - lastWriteTime > appMaxIdleWriteTime()) {
        this@BtSession.stop()
      }
      delay(10000L)
    }
  }

  private suspend fun nextPacket(): NetworkPacket = withContext((Dispatchers.IO)) {
    val length = dataInputStream.readInt()
    val bytes = ByteArray(length - Int.SIZE_BYTES)
    dataInputStream.readFully(bytes)
    val buffer = ByteBuffer.allocate(length)
    buffer.putInt(length).put(bytes).flip()
    val networkPacket = buffer.toNetworkPacket()
    this@BtSession.lastReadTime = System.currentTimeMillis()
    return@withContext networkPacket
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
      nextPacket().also { withContext(Dispatchers.Main) { listener.onNetworkPacket(this@BtSession, it) } }
    }
  }

  fun start() {
    socketJob = coroutineScope.launch(Dispatchers.IO) {
      try {
        closed.store(false)
        handshake()
        readLoop()
      } catch (e: Exception) {
        notifyError(e)
        socketJob = null
        stop()
      }
    }
    watchJob = coroutineScope.launch(Dispatchers.Default) {
      watch()
    }
  }

  suspend fun sendPacket(packet: NetworkPacket): Unit = withContext(Dispatchers.IO) {
    try {
      this@BtSession.lastWriteTime = System.currentTimeMillis()
      writeMutex.withLock { socket.outputStream.apply { write(packet.toByteArray()) }.apply { flush() } }
    } catch (e: Exception) {
      notifyError(e)
      stop()
    }
  }

  suspend fun stop() {
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
