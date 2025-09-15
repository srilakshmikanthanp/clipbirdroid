package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

data class HubMessageNonceChallengeRequestPayload(
  val nonce: String,
) : HubMessagePayload
