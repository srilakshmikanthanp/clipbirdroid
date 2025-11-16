package com.srilakshmikanthanp.clipbirdroid.syncing.network

interface NetRegisterListener {
  fun onServiceUnregistered()
  fun onServiceRegistered()
  fun onServiceRegistrationFailed(errorCode: Int)
  fun onServiceUnregistrationFailed(errorCode: Int)
}
