package com.srilakshmikanthanp.clipbirdroid.syncing.wan

data class WanConnectionState(
  val isConnecting: Boolean = false,
  val isConnected: Boolean = false,
  val error: String? = null
)
