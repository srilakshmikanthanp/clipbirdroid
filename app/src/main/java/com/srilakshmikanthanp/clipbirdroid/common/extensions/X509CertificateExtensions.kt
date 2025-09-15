package com.srilakshmikanthanp.clipbirdroid.common.extensions

import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import java.io.StringWriter
import java.security.cert.X509Certificate

fun X509Certificate.toPem(): String {
  // Create a PemObject containing the certificate data
  val pemObject = PemObject("CERTIFICATE", this.encoded)

  // Write the PemObject to a StringWriter
  val stringWriter = StringWriter()
  PemWriter(stringWriter).use { pemWriter -> pemWriter.writeObject(pemObject) }

  // Return the PEM formatted certificate as a string
  return stringWriter.toString()
}
