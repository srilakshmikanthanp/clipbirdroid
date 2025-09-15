package com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth

import java.time.Instant

data class AuthToken(
  val issuedAt: Instant,
  var token: String,
  val expiry: Instant
)
