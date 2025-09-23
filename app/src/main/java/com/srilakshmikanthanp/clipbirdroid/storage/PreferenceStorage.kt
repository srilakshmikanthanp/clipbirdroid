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
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.srilakshmikanthanp.clipbirdroid.common.functions.jacksonObjectMapperWithTimeModule
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth.AuthToken
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubHostDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferenceStorage(context: Context): SharedPreferences.OnSharedPreferenceChangeListener, Storage {
  private val generalPref = context.getSharedPreferences("GENERAL", Context.MODE_PRIVATE)
  private val hubPref = context.getSharedPreferences("HUB", Context.MODE_PRIVATE)
  private val clientPref = context.getSharedPreferences("CLIENT", Context.MODE_PRIVATE)
  private val serverPref = context.getSharedPreferences("SERVER", Context.MODE_PRIVATE)

  private fun PrivateKey.asString(): String {
    Base64.getEncoder().encode(this.encoded).also { return String(it) }
  }

  private fun String.asPrivateKey(): PrivateKey {
    val bytes = Base64.getDecoder().decode(this)
    val kf = KeyFactory.getInstance("RSA")
    return PKCS8EncodedKeySpec(bytes).let { kf.generatePrivate(it) }
  }

  private fun String.asCertificate(): X509Certificate {
    val certFactory = CertificateFactory.getInstance("X.509")
    val inStream = this.byteInputStream(Charsets.UTF_8)
    return certFactory.generateCertificate(inStream) as X509Certificate
  }

  private fun X509Certificate.asString(): String {
    val stringWriter = StringWriter()
    PemWriter(stringWriter).use {
      it.writeObject(JcaMiscPEMGenerator(this))
    }
    return stringWriter.toString()
  }

  companion object {
    private const val HOST_STATE = "HOST_STATE"
    private const val HOST_KEY = "HOST_KEY"
    private const val HOST_CERT = "HOST_CERT"
    private const val HUB_AUTH_TOKEN = "HUB_AUTH_TOKEN"
    private const val HUB_HOST_DEVICE = "HUB_HOST_DEVICE"
  }

  private val _hostCertFlow = MutableStateFlow(getHostCertificate())
  override val hostCertificateFlow: StateFlow<X509Certificate?> = _hostCertFlow.asStateFlow()

  private val _hostKeyFlow = MutableStateFlow(getHostKey())
  override val hostKeyFlow: StateFlow<PrivateKey?> = _hostKeyFlow.asStateFlow()

  private val _clientCertsFlow = MutableStateFlow(getAllClientCertificate())
  override val clientCertificatesFlow: StateFlow<Map<String, X509Certificate>> = _clientCertsFlow.asStateFlow()

  private val _serverCertsFlow = MutableStateFlow(getAllServerCertificate())
  override val serverCertificatesFlow: StateFlow<Map<String, X509Certificate>> = _serverCertsFlow.asStateFlow()

  private val _hostStateFlow = MutableStateFlow(getHostIsLastlyServer())
  override val hostStateFlow: StateFlow<Boolean> = _hostStateFlow.asStateFlow()

  private val _hubAuthTokenFlow = MutableStateFlow(getHubAuthToken())
  override val hubAuthTokenFlow: StateFlow<AuthToken?> = _hubAuthTokenFlow.asStateFlow()

  private val _hubHostDeviceFlow = MutableStateFlow(getHubHostDevice())
  override val hubHostDeviceFlow: StateFlow<HubHostDevice?> = _hubHostDeviceFlow.asStateFlow()

  init {
    generalPref.registerOnSharedPreferenceChangeListener(this)
    hubPref.registerOnSharedPreferenceChangeListener(this)
  }

  override fun setHostCertificate(cert: X509Certificate) {
    generalPref.edit() { putString(HOST_CERT, cert.asString()) }
  }

  override fun hasHostCert(): Boolean {
    return generalPref.contains(HOST_CERT)
  }

  override fun clearHostCertificate() {
    generalPref.edit() { remove(HOST_CERT) }
  }

  override fun getHostCertificate(): X509Certificate? {
    return generalPref.getString(HOST_CERT, null)?.asCertificate()
  }

  override fun setHostKey(key: PrivateKey) {
    generalPref.edit() { putString(HOST_KEY, key.asString()) }
  }

  override fun hasHostKey(): Boolean {
    return generalPref.contains(HOST_KEY)
  }

  override fun clearHostKey() {
    generalPref.edit() { remove(HOST_KEY) }
  }

  override fun getHostKey(): PrivateKey? {
    return generalPref.getString(HOST_KEY, null)?.asPrivateKey()
  }

  override fun setClientCertificate(name: String, cert: X509Certificate) {
    clientPref.edit() { putString(name, cert.asString()) }
  }

  override fun hasClientCertificate(name: String): Boolean {
    return clientPref.contains(name)
  }

  override fun clearClientCertificate(name: String) {
    clientPref.edit() { remove(name) }
  }

  override fun clearAllClientCertificate() {
    clientPref.edit() { clear() }
  }

  override fun getClientCertificate(name: String): X509Certificate? {
    return clientPref.getString(name, null)?.asCertificate()
  }

  override fun getAllClientCertificate(): Map<String, X509Certificate> {
    return clientPref.all.mapValues { (_, value) -> (value as String).asCertificate() }
  }

  override fun setServerCertificate(name: String, cert: X509Certificate) {
    serverPref.edit() { putString(name, cert.asString()) }
  }

  override fun hasServerCertificate(name: String): Boolean {
    return serverPref.contains(name)
  }

  override fun clearServerCertificate(name: String) {
    serverPref.edit() { remove(name) }
  }

  override fun clearAllServerCertificate() {
    serverPref.edit() { clear() }
  }

  override fun getServerCertificate(name: String): X509Certificate? {
    return serverPref.getString(name, null)?.asCertificate()
  }

  override fun getAllServerCertificate(): Map<String, X509Certificate> {
    return serverPref.all.mapValues { (_, value) -> (value as String).asCertificate() }
  }

  override fun setHostIsLastlyServer(isServer: Boolean) {
    generalPref.edit() { putBoolean(HOST_STATE, isServer) }
  }

  override fun getHostIsLastlyServer(): Boolean {
    return generalPref.getBoolean(HOST_STATE, false)
  }

  override fun setHubAuthToken(token: AuthToken) {
    val objectMapper = jacksonObjectMapperWithTimeModule()
    val json = objectMapper.writeValueAsString(token)
    hubPref.edit() { putString(HUB_AUTH_TOKEN, json) }
  }

  override fun hasHubAuthToken(): Boolean {
    return hubPref.contains(HUB_AUTH_TOKEN)
  }

  override fun getHubAuthToken(): AuthToken? {
    val json = hubPref.getString(HUB_AUTH_TOKEN, null) ?: return null
    val objectMapper = jacksonObjectMapperWithTimeModule()
    return objectMapper.readValue(json, AuthToken::class.java)
  }

  override fun clearHubAuthToken() {
    hubPref.edit() { remove(HUB_AUTH_TOKEN) }
  }

  override fun setHubHostDevice(device: HubHostDevice) {
    val objectMapper = jacksonObjectMapperWithTimeModule()
    val json = objectMapper.writeValueAsString(device)
    hubPref.edit() { putString(HUB_HOST_DEVICE, json) }
  }

  override fun hasHubHostDevice(): Boolean {
    return hubPref.contains(HUB_HOST_DEVICE)
  }

  override fun getHubHostDevice(): HubHostDevice? {
    val json = hubPref.getString(HUB_HOST_DEVICE, null) ?: return null
    val objectMapper = jacksonObjectMapperWithTimeModule()
    return objectMapper.readValue(json, HubHostDevice::class.java)
  }

  override fun clearHubHostDevice() {
    hubPref.edit() { remove(HUB_HOST_DEVICE) }
  }

  override fun onSharedPreferenceChanged(
    sharedPreferences: SharedPreferences,
    key: String?
  ) {
    when (sharedPreferences) {
      generalPref -> {
        when (key) {
          HOST_CERT -> _hostCertFlow.value = getHostCertificate()
          HOST_KEY -> _hostKeyFlow.value = getHostKey()
          HOST_STATE -> _hostStateFlow.value = getHostIsLastlyServer()
        }
      }
      clientPref -> {
        _clientCertsFlow.value = getAllClientCertificate()
      }
      serverPref -> {
        _serverCertsFlow.value = getAllServerCertificate()
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
