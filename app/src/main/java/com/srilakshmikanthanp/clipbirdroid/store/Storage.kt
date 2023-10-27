package com.srilakshmikanthanp.clipbirdroid.store

import android.content.Context
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

class Storage private constructor(context: Context) {
  // Shared Preference for General Group
  private val generalPref = context.getSharedPreferences("GENERAL", Context.MODE_PRIVATE);

  // Shared Preference for Client Group
  private val clientPref = context.getSharedPreferences("CLIENT", Context.MODE_PRIVATE);

  // Shared Preference for Server Group
  private val serverPref = context.getSharedPreferences("SERVER", Context.MODE_PRIVATE);

  // From string to Certificate
  private fun String.toCertificate(): X509Certificate {
    val certFactory = CertificateFactory.getInstance("X.509")
    val inStream = this.byteInputStream(Charsets.UTF_8)
    return certFactory.generateCertificate(inStream) as X509Certificate
  }

  // companion Object
  companion object {
    // Required variables for Singleton
    @Volatile private var instance: Storage? = null
    private val HOST_STATE = "HOST_STATE"

    // Get the instance of Storage
    fun getInstance(context: Context): Storage {
      return instance ?: synchronized(this) {
        instance ?: Storage(context).also { instance = it }
      }
    }
  }

  /**
   * Set the client name and cert
   */
  fun setClientCert(name: String, cert: X509Certificate) {
    clientPref.edit().putString(name, cert.toString()).apply()
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
    clientPref.edit().remove(name).apply()
  }

  /**
   * Clear all client cert
   */
  fun clearAllClientCert() {
    clientPref.edit().clear().apply()
  }

  /**
   * Get the client cert
   */
  fun getClientCert(name: String): X509Certificate? {
    return clientPref.getString(name, null)?.toCertificate()
  }

  /**
   * Get all client cert
   */
  fun getAllClientCert(): List<X509Certificate> {
    return clientPref.all.values.map { (it as String).toCertificate() }
  }

  /**
   * Set the server name and cert
   */
  fun setServerCert(name: String, cert: X509Certificate) {
    serverPref.edit().putString(name, cert.toString()).apply()
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
    serverPref.edit().remove(name).apply()
  }

  /**
   * Clear all server cert
   */
  fun clearAllServerCert() {
    serverPref.edit().clear().apply()
  }

  /**
   * Get the server cert
   */
  fun getServerCert(name: String): X509Certificate? {
    return serverPref.getString(name, null)?.toCertificate()
  }

  /**
   * Get all server cert
   */
  fun getAllServerCert(): List<X509Certificate> {
    return serverPref.all.values.map { (it as String).toCertificate() }
  }

  /**
   * Set the host state
   */
  fun setHostIsLastlyServer(isServer: Boolean) {
    generalPref.edit().putBoolean(HOST_STATE, isServer).apply()
  }

  /**
   * Get the host state
   */
  fun getHostIsLastlyServer(): Boolean {
    return generalPref.getBoolean(HOST_STATE, false)
  }
}
