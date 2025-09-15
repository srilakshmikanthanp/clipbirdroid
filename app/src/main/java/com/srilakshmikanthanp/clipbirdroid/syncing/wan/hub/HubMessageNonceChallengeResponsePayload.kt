package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

data class HubMessageNonceChallengeResponsePayload(
  val signature: String,
  val nonce: String,
) : HubMessagePayload
