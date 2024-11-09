package com.srilakshmikanthanp.clipbirdroid.utilities.functions

import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.constant.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.constant.appName
import com.srilakshmikanthanp.clipbirdroid.constant.appOrgName
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.Date

/**
 * Generate RSA key pair
 */
private fun generateRSAKeyPair(bits: Int): KeyPair {
  val kpg = KeyPairGenerator.getInstance("RSA")
  kpg.initialize(bits)
  return kpg.generateKeyPair()
}

/**
 * Generate X509 certificate
 */
fun generateX509Certificate(context: Context, bits: Int = 1024): Pair<PrivateKey, X509Certificate> {
  // create Expiry dates
  val expiryBefore = Date(System.currentTimeMillis() - (365L * 24L * 60L * 60L * 1000L))
  val expiryAfter = Date(System.currentTimeMillis() + (365L * 24L * 60L * 60L * 1000L))

  // create the names
  val cname = appMdnsServiceName(context)
  val orgName = appOrgName()
  val unit = appName()
  val issuer = X500Name("CN=$cname, O=$orgName, OU=$unit")

  // create the serial
  val serial = System.currentTimeMillis().toBigInteger()

  // create the keypair
  val keyPair = generateRSAKeyPair(bits)
  val pubKey = SubjectPublicKeyInfo.getInstance(keyPair.public.encoded)
  val signer = JcaContentSignerBuilder("SHA256withRSA").build(keyPair.private)

  // create the builder
  val builder = X509v3CertificateBuilder(
    issuer,
    serial,
    expiryBefore,
    expiryAfter,
    issuer,
    pubKey
  )

  // create the certificate
  val cert = JcaX509CertificateConverter().getCertificate(builder.build(signer))

  // return the key pair and certificate
  return Pair(keyPair.private, cert)
}
