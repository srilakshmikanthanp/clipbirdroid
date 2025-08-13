package com.srilakshmikanthanp.clipbirdroid.mdns

interface Register {
  fun addRegisterListener(listener: RegisterListener)
  fun removeRegisterListener(listener: RegisterListener)
  fun registerService(port: Int)
  fun unRegisterService()
  fun reRegister(port: Int)
  fun isRegistered(): Boolean
}
