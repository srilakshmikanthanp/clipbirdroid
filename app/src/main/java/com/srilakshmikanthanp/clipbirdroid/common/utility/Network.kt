package com.srilakshmikanthanp.clipbirdroid.common.utility

import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket

fun getAllInterfaceAddresses(): List<String> {
  val interfaces = NetworkInterface.getNetworkInterfaces()
  val list = mutableListOf<String>()

  val processInterface = { addr: InetAddress ->
    addr.hostAddress?.let { if (!(it.startsWith("127.") || it.contains(":"))) list.add(it) }
  }

  while (interfaces.hasMoreElements()) {
    interfaces.nextElement().inetAddresses.asSequence().forEach { processInterface(it) }
  }

  return list
}

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
