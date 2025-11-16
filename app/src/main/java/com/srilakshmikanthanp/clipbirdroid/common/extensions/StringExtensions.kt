package com.srilakshmikanthanp.clipbirdroid.common.extensions

import java.security.KeyFactory
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

fun String.asCertificate(): X509Certificate {
  val certFactory = CertificateFactory.getInstance("X.509")
  val inStream = this.byteInputStream(Charsets.UTF_8)
  return certFactory.generateCertificate(inStream) as X509Certificate
}


fun String.asPrivateKey(): PrivateKey {
  val bytes = Base64.getDecoder().decode(this)
  val kf = KeyFactory.getInstance("RSA")
  return PKCS8EncodedKeySpec(bytes).let { kf.generatePrivate(it) }
}
