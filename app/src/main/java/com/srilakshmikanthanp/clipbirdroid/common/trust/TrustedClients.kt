package com.srilakshmikanthanp.clipbirdroid.common.trust

import kotlinx.coroutines.flow.StateFlow
import java.security.cert.X509Certificate

interface TrustedClients {
  fun getTrustedClients(): Map<String, X509Certificate>
  fun isTrustedClient(name: String, certificate: X509Certificate): Boolean
  fun addTrustedClient(name: String, certificate: X509Certificate)
  fun removeTrustedClient(name: String)
  val trustedClients: StateFlow<Map<String, X509Certificate>>
}
