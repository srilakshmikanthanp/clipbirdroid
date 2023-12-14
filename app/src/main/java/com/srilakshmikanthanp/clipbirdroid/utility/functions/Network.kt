package com.srilakshmikanthanp.clipbirdroid.utility.functions

import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket


/**
 * Function used to get All Available Network Interface Addresses
 */
fun getAllInterfaceAddresses(): List<String> {
  //  Get all the network interfaces
  val interfaces = NetworkInterface.getNetworkInterfaces()
  val list = mutableListOf<String>()

  // helper to process interface
  val processInterface = { addr: InetAddress ->
    addr.hostAddress?.let { if (!(it.startsWith("127.") || it.contains(":"))) list.add(it) }
  }

  // Iterate all the network interfaces
  while (interfaces.hasMoreElements()) {
    interfaces.nextElement().inetAddresses.asSequence().forEach { processInterface(it) }
  }

  // Return the list
  return list
}

/**
 * Check if host is reachable. it will return true if the host is reachable
 */
fun isHostAvailable(host: InetAddress?, port: Int, timeout: Int = 1000): Boolean {
  Socket().use {
    try {
      it.connect(InetSocketAddress(host, port), timeout)
      return true
    } catch (e: IOException) {
      return false
    }
  }
}
