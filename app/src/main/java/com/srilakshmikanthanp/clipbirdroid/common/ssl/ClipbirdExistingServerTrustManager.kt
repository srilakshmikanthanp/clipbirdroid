package com.srilakshmikanthanp.clipbirdroid.common.ssl

import android.annotation.SuppressLint
import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.storage.Storage
import com.srilakshmikanthanp.clipbirdroid.storage.StorageEntryPoint
import dagger.hilt.android.EntryPointAccessors
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

@SuppressLint("CustomX509TrustManager")
class ClipbirdExistingServerTrustManager(
  private val context: Context
) : X509TrustManager {
  private val storage: Storage by lazy {
    EntryPointAccessors.fromApplication(context.applicationContext, StorageEntryPoint::class.java).storage()
  }

  @SuppressLint("TrustAllX509TrustManager")
  override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    // do nothing
  }

  @SuppressLint("TrustAllX509TrustManager")
  override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    if (chain.isNullOrEmpty()) {
      throw CertificateException("Empty certificate chain")
    }

    val peerCert = chain[0]

    val x500Name = JcaX509CertificateHolder(peerCert).subject
    val rdns = x500Name.getRDNs(BCStyle.CN)

    if (rdns.isEmpty()) {
      throw CertificateException("No CN found in certificate")
    }

    val name = IETFUtils.valueToString(rdns[0].first.value)
    val cert = storage.getServerCertificate(name)

    if (cert == null) {
      throw CertificateException("No certificate found in storage for $name")
    }

    peerCert.checkValidity()

    if (cert != peerCert) {
      throw CertificateException("Certificate mismatch for $name")
    }
  }

  override fun getAcceptedIssuers(): Array<X509Certificate> {
    return arrayOf()
  }
}
