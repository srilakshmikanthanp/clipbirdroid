package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

data class HubHostDevice(
  val id: String,
  val name: String,
  val type: String,
  val publicKey: String,
  val privateKey: String
)
