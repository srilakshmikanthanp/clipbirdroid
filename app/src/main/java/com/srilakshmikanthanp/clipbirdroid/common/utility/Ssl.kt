package com.srilakshmikanthanp.clipbirdroid.common.utility

import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.constants.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.constants.appName
import com.srilakshmikanthanp.clipbirdroid.constants.appOrgName
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.Date

fun generateRSAKeyPair(bits: Int = 2048): KeyPair {
  val kpg = KeyPairGenerator.getInstance("RSA")
  kpg.initialize(bits)
  return kpg.generateKeyPair()
}

fun generateSslConfig(context: Context, bits: Int = 1024): SSLConfig {
  val expiryBefore = Date(System.currentTimeMillis() - (365L * 24L * 60L * 60L * 1000L))
  val expiryAfter = Date(System.currentTimeMillis() + (365L * 24L * 60L * 60L * 1000L))

  val cname = appMdnsServiceName(context)
  val orgName = appOrgName()
  val unit = appName()
  val issuer = X500Name("CN=$cname, O=$orgName, OU=$unit")

  val serial = System.currentTimeMillis().toBigInteger()

  val keyPair = generateRSAKeyPair(bits)
  val pubKey = SubjectPublicKeyInfo.getInstance(keyPair.public.encoded)
  val signer = JcaContentSignerBuilder("SHA256withRSA").build(keyPair.private)

  val builder = X509v3CertificateBuilder(
    issuer,
    serial,
    expiryBefore,
    expiryAfter,
    issuer,
    pubKey
  )

  val cert = JcaX509CertificateConverter().getCertificate(builder.build(signer))
  return SSLConfig(keyPair.private, cert)
}
