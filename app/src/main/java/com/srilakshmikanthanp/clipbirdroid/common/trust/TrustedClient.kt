package com.srilakshmikanthanp.clipbirdroid.common.trust

import java.security.cert.X509Certificate

class TrustedClient(
  val name: String,
  val certificate: X509Certificate
)
