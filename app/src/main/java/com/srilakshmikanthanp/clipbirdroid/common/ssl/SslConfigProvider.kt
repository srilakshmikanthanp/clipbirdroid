package com.srilakshmikanthanp.clipbirdroid.common.ssl

import android.content.Context
import androidx.core.content.edit
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.constants.appCertExpiryInterval
import com.srilakshmikanthanp.clipbirdroid.constants.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.utility.generateSslConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.security.KeyFactory
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SslConfigProvider @Inject constructor(@param:ApplicationContext private val context: Context) {
  private val storagePreference = context.getSharedPreferences(SslConfigProvider::class.simpleName, Context.MODE_PRIVATE)

  companion object {
    private const val HOST_SSL = "HOST_SSL"
  }

  private fun setHostSslConfig(sslConfig: SSLConfig) {
    val privateKeyBase64 = Base64.getEncoder().encodeToString(sslConfig.privateKey.encoded)
    val certificateBase64 = Base64.getEncoder().encodeToString(sslConfig.certificate.encoded)
    val json = JSONObject()
    json.put("certificate", certificateBase64)
    json.put("privateKey", privateKeyBase64)
    storagePreference.edit { putString(HOST_SSL, json.toString()) }
  }

  private fun getHostSslConfig(): SSLConfig? {
    val jsonObject = JSONObject(storagePreference.getString(HOST_SSL, null) ?: return null)
    val certificateBytes = Base64.getDecoder().decode(jsonObject.getString("certificate"))
    val privateKeyBytes = Base64.getDecoder().decode(jsonObject.getString("privateKey"))
    val certificate = CertificateFactory.getInstance("X.509").generateCertificate(ByteArrayInputStream(certificateBytes)) as X509Certificate
    val privateKey = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))
    return SSLConfig(privateKey, certificate)
  }

  fun getSslConfig(): SSLConfig {
    val sslConfig = getHostSslConfig() ?: generateSslConfig(context)
    val currentTime = System.currentTimeMillis()
    if (sslConfig.certificate.notAfter.time - currentTime < appCertExpiryInterval()) return generateSslConfig(context)
    val x500Name = JcaX509CertificateHolder(sslConfig.certificate).subject
    val cn = x500Name.getRDNs(BCStyle.CN)[0]
    val name = IETFUtils.valueToString(cn.first.value)
    val deviceName = appMdnsServiceName(context)
    if (name != deviceName) return generateSslConfig(context)
    setHostSslConfig(sslConfig)
    return sslConfig
  }
}
