package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import android.annotation.SuppressLint

@SuppressLint("MissingPermission")
class BtDeviceConnectionBrowser(private val btDeviceConnectionReceiver: BtDeviceConnectionReceiver) : BtBrowser, BtDeviceConnectionListener {
  private val listeners = mutableSetOf<BtBrowserListener>()
  private val servers: MutableSet<BtResolvedDevice> = mutableSetOf()

  private fun handleDeviceFound(device: BtResolvedDevice) {
    if (servers.add(device)) listeners.forEach { it.onServiceAdded(device) }
  }

  private fun handleDeviceRemoved(device: BtResolvedDevice) {
    if (servers.remove(device)) listeners.forEach { it.onServiceRemoved(device) }
  }

  override fun removeListener(listener: BtBrowserListener) {
    this.listeners.remove(listener)
  }

  override fun addListener(listener: BtBrowserListener) {
    this.listeners.add(listener)
  }

  override fun start() {
    this.btDeviceConnectionReceiver.addListener(this)
    listeners.forEach { it.onBrowsingStarted() }
  }

  override fun stop() {
    this.btDeviceConnectionReceiver.removeListener(this)
    servers.clear()
    listeners.forEach { it.onBrowsingStopped() }
  }

  override fun onDeviceConnected(device: BtResolvedDevice) {
    this.handleDeviceFound(device)
  }

  override fun onDeviceDisconnected(device: BtResolvedDevice) {
    this.handleDeviceRemoved(device)
  }
}
