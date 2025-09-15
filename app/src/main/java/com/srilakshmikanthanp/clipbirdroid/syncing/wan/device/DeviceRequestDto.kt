package com.srilakshmikanthanp.clipbirdroid.syncing.wan.device

data class DeviceRequestDto(
  var publicKey: String,
  var name: String,
  var type: DeviceType,
)
