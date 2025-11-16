package com.srilakshmikanthanp.clipbirdroid.common.trust

import android.annotation.SuppressLint
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

@SuppressLint("CustomX509TrustManager")
class ClipbirdTrustManager(
  private val trustedServers: TrustedServers,
  private val trustedClients: TrustedClients
) : X509TrustManager {
  override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    if (chain.isNullOrEmpty()) throw CertificateException("Empty certificate chain")
    val peerCert = chain[0]
    val x500Name = JcaX509CertificateHolder(peerCert).subject
    val rdns = x500Name.getRDNs(BCStyle.CN)

    if (rdns.isEmpty()) {
      throw CertificateException("No CN found in certificate")
    }

    val name = IETFUtils.valueToString(rdns[0].first.value)

    if (!trustedServers.isTrustedServer(name, peerCert)) {
      throw CertificateException("Server $name is not trusted")
    }

    peerCert.checkValidity()
  }

  override fun getAcceptedIssuers(): Array<X509Certificate> {
    return arrayOf()
  }

  override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    if (chain.isNullOrEmpty()) throw CertificateException("Empty certificate chain")
    val peerCert = chain[0]
    val x500Name = JcaX509CertificateHolder(peerCert).subject
    val rdns = x500Name.getRDNs(BCStyle.CN)

    if (rdns.isEmpty()) {
      throw CertificateException("No CN found in certificate")
    }

    val name = IETFUtils.valueToString(rdns[0].first.value)

    if (!trustedClients.isTrustedClient(name, peerCert)) {
      throw CertificateException("Client $name is not trusted")
    }

    peerCert.checkValidity()
  }
}
