package com.srilakshmikanthanp.clipbirdroid.types.device

import java.net.InetAddress

/**
 * Device class represents the device information
 */
data class Device(
  val ip: InetAddress,
  val port: Int,
  val name: String
)
