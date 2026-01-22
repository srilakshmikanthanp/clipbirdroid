package com.srilakshmikanthanp.clipbirdroid.common.trust

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.security.cert.X509Certificate
import javax.inject.Inject

@HiltViewModel
class TrustedDevicesViewModel @Inject constructor(
  val trustedServers: TrustedServers,
  val trustedClients: TrustedClients
) : ViewModel() {
  val trustedServersFlow = trustedServers.trustedServers
  val trustedClientsFlow = trustedClients.trustedClients

  fun addTrustedServer(name: String, certificate: X509Certificate) {
    viewModelScope.launch { trustedServers.addTrustedServer(TrustedServer(name, certificate)) }
  }

  fun addTrustedClient(name: String, certificate: X509Certificate) {
    viewModelScope.launch { trustedClients.addTrustedClient(TrustedClient(name, certificate)) }
  }

  fun removeTrustedServer(name: String) {
    viewModelScope.launch { trustedServers.removeTrustedServer(name) }
  }

  fun removeTrustedClient(name: String) {
    viewModelScope.launch { trustedClients.removeTrustedClient(name) }
  }
}
