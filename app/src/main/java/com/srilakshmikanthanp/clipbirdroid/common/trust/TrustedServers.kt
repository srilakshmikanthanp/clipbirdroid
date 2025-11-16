package com.srilakshmikanthanp.clipbirdroid.common.trust

import kotlinx.coroutines.flow.StateFlow
import java.security.cert.X509Certificate

interface TrustedServers {
  fun getTrustedServers(): Map<String, X509Certificate>
  fun isTrustedServer(name: String, certificate: X509Certificate): Boolean
  fun hasTrustedServer(name: String): Boolean
  fun addTrustedServer(name: String, certificate: X509Certificate)
  fun removeTrustedServer(name: String)
  val trustedServers: StateFlow<Map<String, X509Certificate>>
}
