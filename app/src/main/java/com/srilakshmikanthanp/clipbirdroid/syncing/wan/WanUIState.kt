package com.srilakshmikanthanp.clipbirdroid.syncing.wan

data class WanUIState(
  val isConnected: Boolean = false,
  val isConnecting: Boolean = false,
  val error: Throwable? = null
)
