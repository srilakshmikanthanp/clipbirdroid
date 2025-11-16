package com.srilakshmikanthanp.clipbirdroid.syncing

import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket
import kotlinx.coroutines.flow.StateFlow
import java.security.cert.X509Certificate

abstract class Session(val name: String) {
  abstract suspend fun sendPacket(packet: NetworkPacket)
  abstract suspend fun disconnect()

  abstract val isTrusted: StateFlow<Boolean>
  abstract fun getCertificate(): X509Certificate

  override fun equals(other: Any?): Boolean {
    if (other !is Session) return false
    if (this === other) return true
    if (name != other.name) return false
    return true
  }

  override fun hashCode(): Int {
    return name.hashCode()
  }
}
