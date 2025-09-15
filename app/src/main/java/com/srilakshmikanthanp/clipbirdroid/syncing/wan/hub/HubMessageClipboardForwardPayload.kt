package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

data class HubMessageClipboardForwardPayload(
  val toDeviceId: String,
  val clipboard: List<Pair<String, ByteArray>>
) : HubMessagePayload
