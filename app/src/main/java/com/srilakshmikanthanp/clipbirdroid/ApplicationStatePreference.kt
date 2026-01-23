package com.srilakshmikanthanp.clipbirdroid

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.security.KeyFactory
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

class ApplicationStatePreference(context: Context): SharedPreferences.OnSharedPreferenceChangeListener, ApplicationState {
  private val storagePreference = context.getSharedPreferences(ApplicationStatePreference::class.simpleName, Context.MODE_PRIVATE)

  private val _hostSslConfigFlow = MutableStateFlow(getHostSslConfig())
  override val hostSslConfigFlow: StateFlow<SSLConfig?> = _hostSslConfigFlow.asStateFlow()

  private val _shouldUseBluetoothFlow = MutableStateFlow(shouldUseBluetooth())
  override val shouldUseBluetoothFlow: StateFlow<Boolean> = _shouldUseBluetoothFlow.asStateFlow()

  private val _isServerFlow = MutableStateFlow(getIsServer())
  override val isServerFlow: StateFlow<Boolean> = _isServerFlow.asStateFlow()

  companion object {
    private const val IS_SERVER = "IS_SERVER"
    private const val SHOULD_USE_BLUETOOTH = "SHOULD_USE_BLUETOOTH"
    private const val HOST_SSL = "HOST_SSL"
    private const val PRIMARY_SERVER = "PRIMARY_SERVER"
  }

  init {
    storagePreference.registerOnSharedPreferenceChangeListener(this)
  }

  override fun removeSslConfig() {
    storagePreference.edit() { remove(HOST_SSL) }
  }

  override fun setHostSslConfig(sslConfig: SSLConfig) {
    val privateKeyBase64 = Base64.getEncoder().encodeToString(sslConfig.privateKey.encoded)
    val certificateBase64 = Base64.getEncoder().encodeToString(sslConfig.certificate.encoded)
    val json = JSONObject().apply {
      put("certificate", certificateBase64)
      put("privateKey", privateKeyBase64)
    }.toString()
    storagePreference.edit { putString(HOST_SSL, json) }
  }

  override fun getHostSslConfig(): SSLConfig? {
    val jsonString = storagePreference.getString(HOST_SSL, null) ?: return null
    val jsonObject = JSONObject(jsonString)
    val certificateBase64 = jsonObject.getString("certificate")
    val privateKeyBase64 = jsonObject.getString("privateKey")
    val certificateBytes = Base64.getDecoder().decode(certificateBase64)
    val privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64)
    val certificate = CertificateFactory.getInstance("X.509").generateCertificate(ByteArrayInputStream(certificateBytes)) as X509Certificate
    val privateKey = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))
    return SSLConfig(privateKey, certificate)
  }

  override fun shouldUseBluetooth(): Boolean {
    return storagePreference.getBoolean(SHOULD_USE_BLUETOOTH, false)
  }

  override fun setShouldUseBluetooth(shouldUseBluetooth: Boolean) {
    storagePreference.edit() { putBoolean(SHOULD_USE_BLUETOOTH, shouldUseBluetooth) }
  }

  override fun setIsServer(isServer: Boolean) {
    storagePreference.edit() { putBoolean(IS_SERVER, isServer) }
  }

  override fun getIsServer(): Boolean {
    return storagePreference.getBoolean(IS_SERVER, false)
  }

  override fun onSharedPreferenceChanged(preference: SharedPreferences?, key: String?) {
    when (key) {
      HOST_SSL -> _hostSslConfigFlow.value = getHostSslConfig()
      SHOULD_USE_BLUETOOTH -> _shouldUseBluetoothFlow.value = shouldUseBluetooth()
      IS_SERVER -> _isServerFlow.value = getIsServer()
    }
  }
}
