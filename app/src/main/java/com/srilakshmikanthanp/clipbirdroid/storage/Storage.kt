package com.srilakshmikanthanp.clipbirdroid.storage

import java.security.PrivateKey
import java.security.cert.X509Certificate
import kotlinx.coroutines.flow.StateFlow
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth.AuthToken
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubHostDevice

interface Storage {
  val clientCertificatesFlow: StateFlow<Map<String, X509Certificate>>
  val serverCertificatesFlow: StateFlow<Map<String, X509Certificate>>
  val hostCertificateFlow: StateFlow<X509Certificate?>
  val hostKeyFlow: StateFlow<PrivateKey?>
  val hostStateFlow: StateFlow<Boolean>
  val hubAuthTokenFlow: StateFlow<AuthToken?>
  val hubHostDeviceFlow: StateFlow<HubHostDevice?>
  val isLastlyConnectedToHubFlow: StateFlow<Boolean>

  fun setHostCertificate(cert: X509Certificate)
  fun hasHostCert(): Boolean
  fun clearHostCertificate()
  fun getHostCertificate(): X509Certificate?

  fun setHostKey(key: PrivateKey)
  fun hasHostKey(): Boolean
  fun clearHostKey()
  fun getHostKey(): PrivateKey?

  fun setClientCertificate(name: String, cert: X509Certificate)
  fun hasClientCertificate(name: String): Boolean
  fun clearClientCertificate(name: String)
  fun clearAllClientCertificate()
  fun getClientCertificate(name: String): X509Certificate?
  fun getAllClientCertificate(): Map<String, X509Certificate>

  fun setServerCertificate(name: String, cert: X509Certificate)
  fun hasServerCertificate(name: String): Boolean
  fun clearServerCertificate(name: String)
  fun clearAllServerCertificate()
  fun getServerCertificate(name: String): X509Certificate?
  fun getAllServerCertificate(): Map<String, X509Certificate>

  fun setHostIsLastlyServer(isServer: Boolean)
  fun getHostIsLastlyServer(): Boolean

  fun setHubAuthToken(token: AuthToken)
  fun hasHubAuthToken(): Boolean
  fun getHubAuthToken(): AuthToken?
  fun clearHubAuthToken()

  fun setHubHostDevice(device: HubHostDevice)
  fun hasHubHostDevice(): Boolean
  fun getHubHostDevice(): HubHostDevice?
  fun clearHubHostDevice()

  fun setISLastlyConnectedToHub(isConnected: Boolean)
  fun getIsLastlyConnectedToHub(): Boolean
}
