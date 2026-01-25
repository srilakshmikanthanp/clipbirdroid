package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import android.annotation.SuppressLint

@SuppressLint("MissingPermission")
class BtDeviceBrowser(private val btDeviceBrowserReceiver: BtDeviceBrowserReceiver) : BtBrowser, BtDeviceBrowserListener {
  private val listeners = mutableSetOf<BtBrowserListener>()
  private val servers: MutableSet<BtResolvedDevice> = mutableSetOf()

  private fun handleDeviceFound(device: BtResolvedDevice) {
    if (servers.add(device)) listeners.forEach { it.onServiceAdded(device) }
  }

  init {
    btDeviceBrowserReceiver.addListener(this)
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
    this.btDeviceBrowserReceiver.start()
    servers.clear()
    listeners.forEach { it.onBrowsingStarted() }
  }

  override fun stop() {
    this.btDeviceBrowserReceiver.stop()
    servers.clear()
    listeners.forEach { it.onBrowsingStopped() }
  }

  override fun onDeviceFound(device: BtResolvedDevice) {
    this.handleDeviceFound(device)
  }

  override fun onDeviceGone(device: BtResolvedDevice) {
    this.handleDeviceRemoved(device)
  }
}
