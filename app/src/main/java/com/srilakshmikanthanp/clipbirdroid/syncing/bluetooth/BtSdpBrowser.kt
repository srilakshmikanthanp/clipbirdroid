package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.ParcelUuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class BtSdpBrowser(private val context: Context, private val coroutineScope: CoroutineScope) : BtBrowser {
  private val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?)?.adapter
  private val listeners = mutableSetOf<BtBrowserListener>()
  private val servers: MutableSet<BtResolvedDevice> = mutableSetOf()
  private var discoveryJob: Job? = null

  private val uuidReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return
      val uuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID) ?: return
      if (uuids.any { it is ParcelUuid && it.uuid == BtConstants.serviceUuid }) {
        handleDeviceFound(BtResolvedDevice(device.name, device.address))
      }
    }
  }

  private fun handleDeviceFound(device: BtResolvedDevice) {
    if (servers.add(device)) listeners.forEach { it.onServiceAdded(device) }
  }

  private suspend fun discoverDevices() {
    while (discoveryJob?.isActive == true) {
      val btAdapter = bluetoothAdapter ?: return
      val paired = btAdapter.bondedDevices
      paired.forEach { it.fetchUuidsWithSdp() }
      delay(10000L)
    }
  }

  override fun removeListener(listener: BtBrowserListener) {
    this.listeners.remove(listener)
  }

  override fun addListener(listener: BtBrowserListener) {
    this.listeners.add(listener)
  }

  override fun start() {
    context.registerReceiver(uuidReceiver, IntentFilter(BluetoothDevice.ACTION_UUID))
    this.discoveryJob = coroutineScope.launch { discoverDevices() }
    listeners.forEach { it.onBrowsingStarted() }
  }

  override fun stop() {
    context.unregisterReceiver(uuidReceiver)
    this.discoveryJob?.cancel()
    servers.clear()
    listeners.forEach { it.onBrowsingStopped() }
  }
}
