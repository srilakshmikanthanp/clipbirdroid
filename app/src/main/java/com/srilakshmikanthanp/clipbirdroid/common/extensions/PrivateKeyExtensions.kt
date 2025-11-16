package com.srilakshmikanthanp.clipbirdroid.common.extensions

import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import java.io.StringWriter
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

fun PrivateKey.asString(): String {
  Base64.getEncoder().encode(this.encoded).also { return String(it) }
}

fun PrivateKey.toPem(): String {
  val pemObject = PemObject("PRIVATE KEY", this.encoded)
  val stringWriter = StringWriter()
  PemWriter(stringWriter).use { it.writeObject(pemObject) }
  return stringWriter.toString()
}
