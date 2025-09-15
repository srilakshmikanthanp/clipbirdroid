package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

data class HubMessageClipboardDispatchPayload(
  val fromDeviceId: String,
  val clipboard: List<Pair<String, ByteArray>>,
) : HubMessagePayload
