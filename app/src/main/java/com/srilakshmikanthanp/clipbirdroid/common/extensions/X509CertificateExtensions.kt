package com.srilakshmikanthanp.clipbirdroid.common.extensions

import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import java.io.StringWriter
import java.security.cert.X509Certificate

fun X509Certificate.asString(): String {
  val stringWriter = StringWriter()
  PemWriter(stringWriter).use {
    it.writeObject(JcaMiscPEMGenerator(this))
  }
  return stringWriter.toString()
}

fun X509Certificate.toPem(): String {
  val pemObject = PemObject("CERTIFICATE", this.encoded)
  val stringWriter = StringWriter()
  PemWriter(stringWriter).use { pemWriter -> pemWriter.writeObject(pemObject) }
  return stringWriter.toString()
}
