package com.srilakshmikanthanp.clipbirdroid.store

import android.content.Context
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator
import org.bouncycastle.util.io.pem.PemWriter
import java.io.StringWriter
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

class Storage private constructor(context: Context) {
  // Shared Preference for General Group
  private val generalPref = context.getSharedPreferences("GENERAL", Context.MODE_PRIVATE)

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
    private val HOST_STATE = "HOST_STATE"
    private val HOST_KEY = "HOST_KEY"
    private val HOST_CERT = "HOST_CERT"

    // Get the instance of Storage
    fun getInstance(context: Context): Storage {
      return instance ?: synchronized(this) {
        instance ?: Storage(context).also { instance = it }
      }
    }
  }

  /**
   * Set the Host Private key and cert
   */
  fun setHostCert(cert: X509Certificate) {
    generalPref.edit().putString(HOST_CERT, cert.asString()).apply()
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
    generalPref.edit().remove(HOST_CERT).apply()
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
    generalPref.edit().putString(HOST_KEY, key.asString()).apply()
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
    generalPref.edit().remove(HOST_KEY).apply()
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
    clientPref.edit().putString(name, cert.asString()).apply()
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
    return clientPref.getString(name, null)?.asCertificate()
  }

  /**
   * Get all client cert
   */
  fun getAllClientCert(): List<X509Certificate> {
    return clientPref.all.values.map { (it as String).asCertificate() }
  }

  /**
   * Set the server name and cert
   */
  fun setServerCert(name: String, cert: X509Certificate) {
    serverPref.edit().putString(name, cert.asString()).apply()
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
    return serverPref.getString(name, null)?.asCertificate()
  }

  /**
   * Get all server cert
   */
  fun getAllServerCert(): List<X509Certificate> {
    return serverPref.all.values.map { (it as String).asCertificate() }
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
