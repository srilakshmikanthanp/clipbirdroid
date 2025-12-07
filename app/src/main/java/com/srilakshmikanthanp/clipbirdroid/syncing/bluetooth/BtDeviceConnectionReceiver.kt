package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BtDeviceConnectionReceiver @Inject constructor(@param:ApplicationContext private val context: Context) : BroadcastReceiver() {
  private val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?)?.adapter
  private val devices: MutableSet<BtResolvedDevice> = mutableSetOf()
  private val listeners = mutableSetOf<BtDeviceConnectionListener>()

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  private fun handleDeviceConnected(device: BluetoothDevice) {
    if (bluetoothAdapter?.bondedDevices?.contains(device) != true) return
    if (!device.uuids.any { it.uuid == BtConstants.serviceUuid }) return
    this.handleDeviceFound(BtResolvedDevice(device.name ?: device.address, device.address))
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  private fun handleDeviceDisconnected(device: BluetoothDevice) {
    this.handleDeviceRemoved(BtResolvedDevice(device.name ?: device.address, device.address))
  }

  private fun handleDeviceFound(device: BtResolvedDevice) {
    devices.add(device).also {listeners.forEach { it.onDeviceConnected(device) } }
  }

  private fun handleDeviceRemoved(device: BtResolvedDevice) {
    devices.remove(device).also {listeners.forEach { it.onDeviceDisconnected(device) } }
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  private fun handleIntent(intent: Intent) {
    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)?.let {
      when (intent.action) {
        BluetoothDevice.ACTION_ACL_CONNECTED -> this.handleDeviceConnected(it)
        BluetoothDevice.ACTION_ACL_DISCONNECTED -> this.handleDeviceDisconnected(it)
      }
    }
  }

  fun addListener(listener: BtDeviceConnectionListener) {
    this.listeners.add(listener).also { devices.forEach { device -> listener.onDeviceConnected(device) } }
  }

  fun removeListener(listener: BtDeviceConnectionListener) {
    this.listeners.remove(listener)
  }

  init {
    IntentFilter().apply {
      addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
      addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
    }.also {
      ContextCompat.registerReceiver(context, this, it, ContextCompat.RECEIVER_EXPORTED)
    }
  }

  override fun onReceive(context: Context, intent: Intent) {
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
      this.handleIntent(intent)
    }
  }
}
