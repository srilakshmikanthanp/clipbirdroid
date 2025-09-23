package com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth

data class AuthUIState(
  val isLoading: Boolean = false,
  val authToken: AuthToken? = null,
  val error: Throwable? = null
)
