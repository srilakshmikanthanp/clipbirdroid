package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import com.srilakshmikanthanp.clipbirdroid.syncing.wan.device.DeviceType

data class HubHostDevice(
  val id: String,
  val name: String,
  val type: DeviceType,
  val publicKey: String,
  val privateKey: String
)
