package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import android.Manifest
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BtDeviceBrowserReceiver @Inject constructor(
  @param:ApplicationContext private val context: Context
) : BroadcastReceiver() {
  private val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?)?.adapter
  private val listeners = mutableSetOf<BtDeviceBrowserListener>()
  private val devices: MutableSet<BtResolvedDevice> = mutableSetOf()

  @RequiresPermission(BLUETOOTH_CONNECT)
  private fun handleDeviceFound(device: BluetoothDevice) {
    if (device.uuids?.any { it.uuid == BtConstants.serviceUuid } == true) {
      this.handleDeviceFound(BtResolvedDevice(device.name ?: device.address, device.address))
    } else {
      device.fetchUuidsWithSdp()
    }
  }

  private fun handleDeviceFound(device: BtResolvedDevice) {
    devices.add(device).also { listeners.forEach { it.onDeviceFound(device) } }
  }

  @RequiresPermission(BLUETOOTH_CONNECT)
  private fun handleUuids(device: BluetoothDevice) {
    if (device.bondState == BluetoothDevice.BOND_BONDED && device.uuids?.any { it.uuid == BtConstants.serviceUuid } == true) {
      this.handleDeviceFound(BtResolvedDevice(device.name ?: device.address, device.address))
    }
  }

  @RequiresPermission(BLUETOOTH_CONNECT)
  private fun operateBondedDevices() {
    bluetoothAdapter?.bondedDevices?.forEach {
      if (it.uuids != null) {
        this.handleUuids(it)
      } else {
        it.fetchUuidsWithSdp()
      }
    }
  }

  @RequiresPermission(BLUETOOTH_CONNECT)
  private fun handleIntent(intent: Intent) {
    if (intent.action == BluetoothDevice.ACTION_ACL_CONNECTED) {
      this.handleDeviceFound(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) ?: return)
    }
    if (intent.action == Intent.ACTION_USER_PRESENT) {
      this.operateBondedDevices()
    }
    if (intent.action == BluetoothDevice.ACTION_UUID) {
      this.handleUuids(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) ?: return)
    }
  }

  fun addListener(listener: BtDeviceBrowserListener) {
    this.listeners.add(listener).also { devices.forEach { device -> listener.onDeviceFound(device) } }
  }

  fun removeListener(listener: BtDeviceBrowserListener) {
    this.listeners.remove(listener)
  }

  fun start() {
    IntentFilter().apply {
      addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
      addAction(BluetoothDevice.ACTION_UUID)
      addAction(Intent.ACTION_USER_PRESENT)
    }.also {
      ContextCompat.registerReceiver(context, this, it, ContextCompat.RECEIVER_EXPORTED)
    }
    if (checkSelfPermission(context, BLUETOOTH_CONNECT) == PERMISSION_GRANTED) {
      this.operateBondedDevices()
    }
  }

  fun stop() {
    context.unregisterReceiver(this)
  }

  override fun onReceive(context: Context, intent: Intent) {
    if (checkSelfPermission(context, BLUETOOTH_CONNECT) == PERMISSION_GRANTED) {
      this.handleIntent(intent)
    }
  }
}
