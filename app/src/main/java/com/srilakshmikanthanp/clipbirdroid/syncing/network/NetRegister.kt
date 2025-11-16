package com.srilakshmikanthanp.clipbirdroid.syncing.network

interface NetRegister {
  fun addRegisterListener(listener: NetRegisterListener)
  fun removeRegisterListener(listener: NetRegisterListener)
  fun registerService(port: Int)
  fun unregisterService()
}
