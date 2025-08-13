package com.srilakshmikanthanp.clipbirdroid.mdns

interface RegisterListener {
  fun onServiceUnregistered()
  fun onServiceRegistered()
  fun onServiceRegistrationFailed(errorCode: Int)
  fun onServiceUnregistrationFailed(errorCode: Int)
}
