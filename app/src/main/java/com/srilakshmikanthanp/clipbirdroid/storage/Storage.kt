package com.srilakshmikanthanp.clipbirdroid.storage

import java.security.PrivateKey
import java.security.cert.X509Certificate
import kotlinx.coroutines.flow.StateFlow
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth.AuthToken
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubHostDevice

interface Storage {
  val hostCertFlow: StateFlow<X509Certificate?>
  val hostKeyFlow: StateFlow<PrivateKey?>
  val clientCertsFlow: StateFlow<Map<String, X509Certificate>>
  val serverCertsFlow: StateFlow<Map<String, X509Certificate>>
  val hostStateFlow: StateFlow<Boolean>
  val hubAuthTokenFlow: StateFlow<AuthToken?>
  val hubHostDeviceFlow: StateFlow<HubHostDevice?>

  fun setHostCert(cert: X509Certificate)
  fun hasHostCert(): Boolean
  fun clearHostCert()
  fun getHostCert(): X509Certificate?

  fun setHostKey(key: PrivateKey)
  fun hasHostKey(): Boolean
  fun clearHostKey()
  fun getHostKey(): PrivateKey?

  fun setClientCert(name: String, cert: X509Certificate)
  fun hasClientCert(name: String): Boolean
  fun clearClientCert(name: String)
  fun clearAllClientCert()
  fun getClientCert(name: String): X509Certificate?
  fun getAllClientCert(): Map<String, X509Certificate>

  fun setServerCert(name: String, cert: X509Certificate)
  fun hasServerCert(name: String): Boolean
  fun clearServerCert(name: String)
  fun clearAllServerCert()
  fun getServerCert(name: String): X509Certificate?
  fun getAllServerCert(): Map<String, X509Certificate>

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
}
