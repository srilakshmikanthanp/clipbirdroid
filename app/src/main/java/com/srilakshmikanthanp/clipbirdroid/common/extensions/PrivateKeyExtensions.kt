package com.srilakshmikanthanp.clipbirdroid.common.extensions

import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import java.io.StringWriter
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec

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
