package com.srilakshmikanthanp.clipbirdroid.utility.functions

import java.net.NetworkInterface

/**
 * Function used to get All Available Network Interface Addresses
 */
fun getAllInterfaceAddresses(): List<String> {
  //  Get all the network interfaces
  val interfaces = NetworkInterface.getNetworkInterfaces()
  val list = mutableListOf<String>()

  // Iterate all the network interfaces
  while (interfaces.hasMoreElements()) {
    val addresses = interfaces.nextElement().inetAddresses
    addresses.asSequence().forEach { it.hostAddress?.let { a -> list.add(a) } }
  }

  // Return the list
  return list
}
