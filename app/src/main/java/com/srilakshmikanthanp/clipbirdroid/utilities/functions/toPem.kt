package com.srilakshmikanthanp.clipbirdroid.utilities.functions

import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import java.io.StringWriter
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec

/**
 * Function used to convert the Private Key to PEM
 */
fun PrivateKey.toPem(): String {
  // Create a PKCS8EncodedKeySpec
  val keySpec = PKCS8EncodedKeySpec(this.encoded)

  // Create a KeyFactory instance for RSA
  val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")

  // Generate the private key from the key specification
  val rsaPrivateKey: PrivateKey = keyFactory.generatePrivate(keySpec)

  // Create a PemObject containing the private key data
  val pemObject = PemObject("PRIVATE KEY", rsaPrivateKey.encoded)

  // Write the PemObject to a StringWriter
  val stringWriter = StringWriter()
  PemWriter(stringWriter).use { pemWriter -> pemWriter.writeObject(pemObject) }

  // Return the PEM formatted private key as a string
  return stringWriter.toString()
}

/**
 * Function used to convert the Certificate to PEM
 */
fun X509Certificate.toPem(): String {
  // Create a PemObject containing the certificate data
  val pemObject = PemObject("CERTIFICATE", this.encoded)

  // Write the PemObject to a StringWriter
  val stringWriter = StringWriter()
  PemWriter(stringWriter).use { pemWriter -> pemWriter.writeObject(pemObject) }

  // Return the PEM formatted certificate as a string
  return stringWriter.toString()
}
