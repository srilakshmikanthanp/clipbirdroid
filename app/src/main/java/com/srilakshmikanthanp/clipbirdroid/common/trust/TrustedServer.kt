package com.srilakshmikanthanp.clipbirdroid.common.trust

import java.security.cert.X509Certificate

class TrustedServer(
  val name: String,
  val certificate: X509Certificate
)
