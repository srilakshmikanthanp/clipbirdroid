package com.srilakshmikanthanp.clipbirdroid

import kotlinx.coroutines.flow.StateFlow

interface ApplicationState {
  val shouldUseBluetoothFlow: StateFlow<Boolean>
  val isServerFlow: StateFlow<Boolean>
  val primaryServerFlow: StateFlow<String?>

  fun shouldUseBluetooth(): Boolean
  fun setShouldUseBluetooth(shouldUseBluetooth: Boolean)

  fun setIsServer(isServer: Boolean)
  fun getIsServer(): Boolean

  fun setLastConnectedServer(name: String)
  fun removeLastConnectedServer()
  fun getLastConnectedServer(): String?
}
