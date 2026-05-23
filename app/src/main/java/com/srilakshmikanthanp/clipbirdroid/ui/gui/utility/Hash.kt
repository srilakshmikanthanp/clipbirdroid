package com.srilakshmikanthanp.clipbirdroid.ui.gui.utility

import java.security.MessageDigest
import java.security.cert.X509Certificate

fun sha256Fingerprint(cert: X509Certificate): String {
  return MessageDigest.getInstance("SHA-256").digest(cert.encoded).joinToString(":") { "%02X".format(it) }
}
