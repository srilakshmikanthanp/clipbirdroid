package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

data class HubMessage<T: HubMessagePayload> (
  val type: HubMessageType,
  val payload: T
)
