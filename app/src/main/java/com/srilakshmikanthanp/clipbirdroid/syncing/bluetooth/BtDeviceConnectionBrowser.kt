package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.CoroutineScope

@SuppressLint("MissingPermission")
class BtDeviceConnectionBrowser(private val context: Context, parentScope: CoroutineScope) : BtBrowser, BroadcastReceiver() {
  private val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?)?.adapter
  private val listeners = mutableSetOf<BtBrowserListener>()
  private val servers: MutableSet<BtResolvedDevice> = mutableSetOf()

  private fun handleDeviceConnected(device: BluetoothDevice) {
    if (bluetoothAdapter?.bondedDevices?.contains(device) != true) return
    if (!device.uuids.any { it.uuid == BtConstants.serviceUuid }) return
    this.handleDeviceFound(BtResolvedDevice(device.name ?: device.address, device.address))
  }

  private fun handleDeviceDisconnected(device: BluetoothDevice) {
    this.handleDeviceRemoved(BtResolvedDevice(device.name ?: device.address, device.address))
  }

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
    val intentFilter = IntentFilter()
    intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
    intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
    context.registerReceiver(this, intentFilter)
    listeners.forEach { it.onBrowsingStarted() }
  }

  override fun stop() {
    context.unregisterReceiver(this)
    servers.clear()
    listeners.forEach { it.onBrowsingStopped() }
  }

  override fun onReceive(context: Context, intent: Intent) {
    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return
    when (intent.action) {
      BluetoothDevice.ACTION_ACL_CONNECTED -> this.handleDeviceConnected(device)
      BluetoothDevice.ACTION_ACL_DISCONNECTED -> this.handleDeviceDisconnected(device)
    }
  }
}
