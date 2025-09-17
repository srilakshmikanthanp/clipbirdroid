package com.srilakshmikanthanp.clipbirdroid.storage

import android.content.Context
import android.content.SharedPreferences
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator
import org.bouncycastle.util.io.pem.PemWriter
import java.io.StringWriter
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import androidx.core.content.edit
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth.AuthToken
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubHostDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class Storage private constructor(context: Context): SharedPreferences.OnSharedPreferenceChangeListener {
  // Shared Preference for General Group
  private val generalPref = context.getSharedPreferences("GENERAL", Context.MODE_PRIVATE)

  // Shared Preference for Hub Group
  private val hubPref = context.getSharedPreferences("HUB", Context.MODE_PRIVATE)

  // Shared Preference for Client Group
  private val clientPref = context.getSharedPreferences("CLIENT", Context.MODE_PRIVATE)

  // Shared Preference for Server Group
  private val serverPref = context.getSharedPreferences("SERVER", Context.MODE_PRIVATE)

  // From PrivateKey to string
  private fun PrivateKey.asString(): String {
    Base64.getEncoder().encode(this.encoded).also { return String(it) }
  }

  // From string to PrivateKey
  private fun String.asPrivateKey(): PrivateKey {
    val bytes = Base64.getDecoder().decode(this)
    val kf = KeyFactory.getInstance("RSA")
    return PKCS8EncodedKeySpec(bytes).let { kf.generatePrivate(it) }
  }

  // From string to Certificate
  private fun String.asCertificate(): X509Certificate {
    val certFactory = CertificateFactory.getInstance("X.509")
    val inStream = this.byteInputStream(Charsets.UTF_8)
    return certFactory.generateCertificate(inStream) as X509Certificate
  }

  // From Certificate to string
  private fun X509Certificate.asString(): String {
    val stringWriter = StringWriter()
    PemWriter(stringWriter).use {
      it.writeObject(JcaMiscPEMGenerator(this))
    }
    return stringWriter.toString()
  }

  // companion Object
  companion object {
    // Required variables for Singleton
    @Volatile private var instance: Storage? = null
    private const val HOST_STATE = "HOST_STATE"
    private const val HOST_KEY = "HOST_KEY"
    private const val HOST_CERT = "HOST_CERT"
    private const val HUB_AUTH_TOKEN = "HUB_AUTH_TOKEN"
    private const val HUB_HOST_DEVICE = "HUB_HOST_DEVICE"

    // Get the instance of Storage
    fun getInstance(context: Context): Storage {
      return instance ?: synchronized(this) {
        instance ?: Storage(context).also { instance = it }
      }
    }
  }

  // Flows for observing changes
  private val _hostCertFlow = MutableStateFlow(getHostCert())
  val hostCertFlow: StateFlow<X509Certificate?> = _hostCertFlow.asStateFlow()

  private val _hostKeyFlow = MutableStateFlow(getHostKey())
  val hostKeyFlow: StateFlow<PrivateKey?> = _hostKeyFlow.asStateFlow()

  private val _clientCertsFlow = MutableStateFlow(getAllClientCert())
  val clientCertsFlow: StateFlow<Map<String, X509Certificate>> = _clientCertsFlow.asStateFlow()

  private val _serverCertsFlow = MutableStateFlow(getAllServerCert())
  val serverCertsFlow: StateFlow<Map<String, X509Certificate>> = _serverCertsFlow.asStateFlow()

  private val _hostStateFlow = MutableStateFlow(getHostIsLastlyServer())
  val hostStateFlow: StateFlow<Boolean> = _hostStateFlow.asStateFlow()

  private val _hubAuthTokenFlow = MutableStateFlow(getHubAuthToken())
  val hubAuthTokenFlow: StateFlow<AuthToken?> = _hubAuthTokenFlow.asStateFlow()

  private val _hubHostDeviceFlow = MutableStateFlow(getHubHostDevice())
  val hubHostDeviceFlow: StateFlow<HubHostDevice?> = _hubHostDeviceFlow.asStateFlow()

  init {
    generalPref.registerOnSharedPreferenceChangeListener(this)
    hubPref.registerOnSharedPreferenceChangeListener(this)
  }

  /**
   * Set the Host Private key and cert
   */
  fun setHostCert(cert: X509Certificate) {
    generalPref.edit() { putString(HOST_CERT, cert.asString()) }
  }

  /**
   * Check the Host cert is available
   */
  fun hasHostCert(): Boolean {
    return generalPref.contains(HOST_CERT)
  }

  /**
   * Clear the Host cert
   */
  fun clearHostCert() {
    generalPref.edit() { remove(HOST_CERT) }
  }

  /**
   * Get the Host cert
   */
  fun getHostCert(): X509Certificate? {
    return generalPref.getString(HOST_CERT, null)?.asCertificate()
  }

  /**
   * Set the Host Private key
   */
  fun setHostKey(key: PrivateKey) {
    generalPref.edit() { putString(HOST_KEY, key.asString()) }
  }

  /**
   * Check the Host key is available
   */
  fun hasHostKey(): Boolean {
    return generalPref.contains(HOST_KEY)
  }

  /**
   * Clear the Host key
   */
  fun clearHostKey() {
    generalPref.edit() { remove(HOST_KEY) }
  }

  /**
   * Get the Host key
   */
  fun getHostKey(): PrivateKey? {
    return generalPref.getString(HOST_KEY, null)?.asPrivateKey()
  }


  /**
   * Set the client name and cert
   */
  fun setClientCert(name: String, cert: X509Certificate) {
    clientPref.edit() { putString(name, cert.asString()) }
  }

  /**
   * Check the client cert is available
   */
  fun hasClientCert(name: String): Boolean {
    return clientPref.contains(name)
  }

  /**
   * Clear the client cert
   */
  fun clearClientCert(name: String) {
    clientPref.edit() { remove(name) }
  }

  /**
   * Clear all client cert
   */
  fun clearAllClientCert() {
    clientPref.edit() { clear() }
  }

  /**
   * Get the client cert
   */
  fun getClientCert(name: String): X509Certificate? {
    return clientPref.getString(name, null)?.asCertificate()
  }

  /**
   * Get all client cert
   */
  fun getAllClientCert(): Map<String, X509Certificate> {
    return clientPref.all.mapValues { (_, value) -> (value as String).asCertificate() }
  }

  /**
   * Set the server name and cert
   */
  fun setServerCert(name: String, cert: X509Certificate) {
    serverPref.edit() { putString(name, cert.asString()) }
  }

  /**
   * Check the server cert is available
   */
  fun hasServerCert(name: String): Boolean {
    return serverPref.contains(name)
  }

  /**
   * Clear the server cert
   */
  fun clearServerCert(name: String) {
    serverPref.edit() { remove(name) }
  }

  /**
   * Clear all server cert
   */
  fun clearAllServerCert() {
    serverPref.edit() { clear() }
  }

  /**
   * Get the server cert
   */
  fun getServerCert(name: String): X509Certificate? {
    return serverPref.getString(name, null)?.asCertificate()
  }

  /**
   * Get all server cert
   */
  fun getAllServerCert(): Map<String, X509Certificate> {
    return serverPref.all.mapValues { (_, value) -> (value as String).asCertificate() }
  }

  /**
   * Set the host state
   */
  fun setHostIsLastlyServer(isServer: Boolean) {
    generalPref.edit() { putBoolean(HOST_STATE, isServer) }
  }

  /**
   * Get the host state
   */
  fun getHostIsLastlyServer(): Boolean {
    return generalPref.getBoolean(HOST_STATE, false)
  }

  fun setHubAuthToken(token: AuthToken) {
    val objectMapper = jacksonObjectMapper()
    val json = objectMapper.writeValueAsString(token)
    hubPref.edit() { putString(HUB_AUTH_TOKEN, json) }
  }

  fun hasHubAuthToken(): Boolean {
    return hubPref.contains(HUB_AUTH_TOKEN)
  }

  fun getHubAuthToken(): AuthToken? {
    val json = hubPref.getString(HUB_AUTH_TOKEN, null) ?: return null
    val objectMapper = jacksonObjectMapper()
    return objectMapper.readValue(json, AuthToken::class.java)
  }

  fun clearHubAuthToken() {
    hubPref.edit() { remove(HUB_AUTH_TOKEN) }
  }

  fun setHubHostDevice(device: HubHostDevice) {
    val objectMapper = jacksonObjectMapper()
    val json = objectMapper.writeValueAsString(device)
    hubPref.edit() { putString(HUB_HOST_DEVICE, json) }
  }

  fun hasHubHostDevice(): Boolean {
    return hubPref.contains(HUB_HOST_DEVICE)
  }

  fun getHubHostDevice(): HubHostDevice? {
    val json = hubPref.getString(HUB_HOST_DEVICE, null) ?: return null
    val objectMapper = jacksonObjectMapper()
    return objectMapper.readValue(json, HubHostDevice::class.java)
  }

  fun clearHubHostDevice() {
    hubPref.edit() { remove(HUB_HOST_DEVICE) }
  }

  override fun onSharedPreferenceChanged(
    sharedPreferences: SharedPreferences,
    key: String?
  ) {
    when (sharedPreferences) {
      generalPref -> {
        when (key) {
          HOST_CERT -> _hostCertFlow.value = getHostCert()
          HOST_KEY -> _hostKeyFlow.value = getHostKey()
          HOST_STATE -> _hostStateFlow.value = getHostIsLastlyServer()
        }
      }
      clientPref -> {
        _clientCertsFlow.value = getAllClientCert()
      }
      serverPref -> {
        _serverCertsFlow.value = getAllServerCert()
      }
      hubPref -> {
        when (key) {
          HUB_AUTH_TOKEN -> _hubAuthTokenFlow.value = getHubAuthToken()
          HUB_HOST_DEVICE -> _hubHostDeviceFlow.value = getHubHostDevice()
        }
      }
    }
  }
}
