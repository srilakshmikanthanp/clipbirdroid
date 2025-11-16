package com.srilakshmikanthanp.clipbirdroid.common.trust

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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
    trustedServers.addTrustedServer(name, certificate)
  }

  fun addTrustedClient(name: String, certificate: X509Certificate) {
    trustedClients.addTrustedClient(name, certificate)
  }

  fun removeTrustedServer(name: String) {
    trustedServers.removeTrustedServer(name)
  }

  fun removeTrustedClient(name: String) {
    trustedClients.removeTrustedClient(name)
  }
}
