package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class BtSdpBrowser(private val context: Context, parentScope: CoroutineScope) : BtBrowser {
  private val coroutineScope = CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]))
  private val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?)?.adapter
  private val listeners = mutableSetOf<BtBrowserListener>()
  private val servers: MutableSet<BtResolvedDevice> = mutableSetOf()
  private var discoveryJob: Job? = null

  private fun handlePairedDevices(devices: Collection<BluetoothDevice>) {
    devices.forEach { btDevice ->
      val uuids = btDevice.uuids?.map { it.uuid } ?: emptyList()
      if (BtConstants.serviceUuid.let { uuids.contains(it) }) {
        handleDeviceFound(BtResolvedDevice(btDevice.name ?: btDevice.address, btDevice.address))
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
      handlePairedDevices(paired)
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
    this.discoveryJob = coroutineScope.launch { discoverDevices() }
    listeners.forEach { it.onBrowsingStarted() }
  }

  override fun stop() {
    this.discoveryJob?.cancel()
    servers.clear()
    listeners.forEach { it.onBrowsingStopped() }
  }
}
