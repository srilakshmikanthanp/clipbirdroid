package com.srilakshmikanthanp.clipbirdroid.common.types

import java.io.Serializable
import java.net.InetAddress

/**
 * Device class represents the device information
 */
data class Device(
  val ip: InetAddress,
  val port: Int,
  val name: String,
) : Serializable {
  companion object {
    private const val serialVersionUID = 1L
  }
}
