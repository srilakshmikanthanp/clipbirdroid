package com.srilakshmikanthanp.clipbirdroid

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
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
  
  private val _shouldUseBluetoothFlow = MutableStateFlow(shouldUseBluetooth())
  override val shouldUseBluetoothFlow: StateFlow<Boolean> = _shouldUseBluetoothFlow.asStateFlow()

  private val _isServerFlow = MutableStateFlow(getIsServer())
  override val isServerFlow: StateFlow<Boolean> = _isServerFlow.asStateFlow()

  private val _primaryServerFlow = MutableStateFlow(storagePreference.getString(PRIMARY_SERVER, null))
  override val primaryServerFlow: StateFlow<String?> = _primaryServerFlow.asStateFlow()

  companion object {
    private const val IS_SERVER = "IS_SERVER"
    private const val SHOULD_USE_BLUETOOTH = "SHOULD_USE_BLUETOOTH"
    private const val PRIMARY_SERVER = "PRIMARY_SERVER"
  }

  init {
    storagePreference.registerOnSharedPreferenceChangeListener(this)
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

  override fun setLastConnectedServer(name: String) {
    storagePreference.edit() { putString(PRIMARY_SERVER, name) }
  }

  override fun getLastConnectedServer(): String? {
    return storagePreference.getString(PRIMARY_SERVER, null)
  }

  override fun removeLastConnectedServer() {
    storagePreference.edit() { remove(PRIMARY_SERVER) }
  }

  override fun onSharedPreferenceChanged(preference: SharedPreferences?, key: String?) {
    when (key) {
      SHOULD_USE_BLUETOOTH -> _shouldUseBluetoothFlow.value = shouldUseBluetooth()
      IS_SERVER -> _isServerFlow.value = getIsServer()
      PRIMARY_SERVER -> _primaryServerFlow.value = getLastConnectedServer()
    }
  }
}
