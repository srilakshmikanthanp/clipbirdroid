package com.srilakshmikanthanp.clipbirdroid

import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import kotlinx.coroutines.flow.StateFlow

interface ApplicationState {
  val hostSslConfigFlow: StateFlow<SSLConfig?>
  val shouldUseBluetoothFlow: StateFlow<Boolean>
  val isServerFlow: StateFlow<Boolean>
  val primaryServerFlow: StateFlow<String?>

  fun setHostSslConfig(sslConfig: SSLConfig)
  fun removeSslConfig()
  fun getHostSslConfig(): SSLConfig?

  fun shouldUseBluetooth(): Boolean
  fun setShouldUseBluetooth(shouldUseBluetooth: Boolean)

  fun setIsServer(isServer: Boolean)
  fun getIsServer(): Boolean

  fun setPrimaryServer(name: String)
  fun removePrimaryServer()
  fun getPrimaryServer(): String?
}
