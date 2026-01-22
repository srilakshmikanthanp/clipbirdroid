package com.srilakshmikanthanp.clipbirdroid.common.trust

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.security.cert.X509Certificate

interface TrustedServers {
  suspend fun getTrustedServers(): List<TrustedServer>
  suspend fun isTrustedServer(server: TrustedServer): Boolean
  suspend fun hasTrustedServer(name: String): Boolean
  suspend fun addTrustedServer(server: TrustedServer)
  suspend fun removeTrustedServer(name: String)
  val trustedServers: Flow<List<TrustedServer>>
}
