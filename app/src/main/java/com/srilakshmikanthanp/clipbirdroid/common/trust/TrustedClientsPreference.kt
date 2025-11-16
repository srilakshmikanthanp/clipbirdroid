package com.srilakshmikanthanp.clipbirdroid.common.trust

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.srilakshmikanthanp.clipbirdroid.common.extensions.asCertificate
import com.srilakshmikanthanp.clipbirdroid.common.extensions.toPem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.cert.X509Certificate
import java.util.prefs.PreferenceChangeEvent
import java.util.prefs.PreferenceChangeListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrustedClientsPreference @Inject constructor(private val context: Context): TrustedClients, SharedPreferences.OnSharedPreferenceChangeListener {
  private val clientCertificatePreference = context.getSharedPreferences(TrustedClientsPreference::class.simpleName, Context.MODE_PRIVATE)

  private val _trustedClients: MutableStateFlow<Map<String, X509Certificate>> = MutableStateFlow(getTrustedClients())
  override val trustedClients: StateFlow<Map<String, X509Certificate>> = _trustedClients.asStateFlow()

  init {
    clientCertificatePreference.registerOnSharedPreferenceChangeListener(this)
  }

  override fun getTrustedClients(): Map<String, X509Certificate> {
    return clientCertificatePreference.all.mapNotNull { (name, certString) ->
      val certificate = (certString as? String)?.asCertificate() ?: return@mapNotNull null
      name to certificate
    }.toMap()
  }

  override fun isTrustedClient(name: String, certificate: X509Certificate): Boolean {
    return clientCertificatePreference.getString(name, null)?.asCertificate() == certificate
  }

  override fun addTrustedClient(name: String, certificate: X509Certificate) {
    clientCertificatePreference.edit { putString(name, certificate.toPem()) }
  }

  override fun removeTrustedClient(name: String) {
    clientCertificatePreference.edit { remove(name) }
  }

  override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
    _trustedClients.value = getTrustedClients()
  }
}
