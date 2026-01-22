package com.srilakshmikanthanp.clipbirdroid.common.trust

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.security.cert.X509Certificate

interface TrustedClients {
  suspend fun getTrustedClients(): List<TrustedClient>
  suspend fun isTrustedClient(client: TrustedClient): Boolean
  suspend fun addTrustedClient(client: TrustedClient)
  suspend fun removeTrustedClient(name: String)
  val trustedClients: Flow<List<TrustedClient>>
}
