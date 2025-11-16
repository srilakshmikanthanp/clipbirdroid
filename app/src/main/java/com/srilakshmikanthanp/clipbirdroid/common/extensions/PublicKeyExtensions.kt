package com.srilakshmikanthanp.clipbirdroid.common.extensions

import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import java.io.StringWriter
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec

fun PublicKey.toPem(): String {
  val pemObject = PemObject("PUBLIC KEY", this.encoded)
  val stringWriter = StringWriter()
  PemWriter(stringWriter).use { it.writeObject(pemObject) }
  return stringWriter.toString()
}
