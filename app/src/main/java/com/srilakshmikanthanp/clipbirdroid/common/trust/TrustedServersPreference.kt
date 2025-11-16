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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrustedServersPreference @Inject constructor(private val context: Context): TrustedServers, SharedPreferences.OnSharedPreferenceChangeListener {
  private val serverCertificatePreference = context.getSharedPreferences(TrustedServersPreference::class.simpleName, Context.MODE_PRIVATE)

  private val _trustedServers: MutableStateFlow<Map<String, X509Certificate>> = MutableStateFlow(getTrustedServers())
  override val trustedServers: StateFlow<Map<String, X509Certificate>> = _trustedServers.asStateFlow()

  init {
    serverCertificatePreference.registerOnSharedPreferenceChangeListener(this)
  }

  override fun getTrustedServers(): Map<String, X509Certificate> {
    return serverCertificatePreference.all.mapNotNull { (name, certString) ->
      val certificate = (certString as? String)?.asCertificate() ?: return@mapNotNull null
      name to certificate
    }.toMap()
  }

  override fun isTrustedServer(name: String, certificate: X509Certificate): Boolean {
    return serverCertificatePreference.getString(name, null)?.asCertificate() == certificate
  }

  override fun hasTrustedServer(name: String): Boolean {
    return serverCertificatePreference.contains(name)
  }

  override fun addTrustedServer(name: String, certificate: X509Certificate) {
    serverCertificatePreference.edit { putString(name, certificate.toPem()) }
  }

  override fun removeTrustedServer(name: String) {
    serverCertificatePreference.edit { remove(name) }
  }

  override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
    _trustedServers.value = getTrustedServers()
  }
}
