package com.srilakshmikanthanp.clipbirdroid.common.types

import java.security.PrivateKey
import java.security.cert.X509Certificate

data class SSLConfig(
  val privateKey: PrivateKey,
  val certificate: X509Certificate
)
