package com.srilakshmikanthanp.clipbirdroid.mdns

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.PROTOCOL_DNS_SD
import android.net.nsd.NsdManager.RegistrationListener
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.MulticastLock
import android.util.Log
import com.srilakshmikanthanp.clipbirdroid.constant.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.constant.appMdnsServiceType
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class Register(private val context: Context) : RegistrationListener {
  private val nsdManager: NsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

  private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

  private val listeners: MutableList<RegisterListener> = mutableListOf()

  private var isRegistered: AtomicBoolean = AtomicBoolean(false)

  private var pendingRestartPort: AtomicInteger = AtomicInteger(-1)

  private val multicastLock: MulticastLock = wifiManager.createMulticastLock(MULTICAST_LOCK_TAG)

  // TAG for logging.
  companion object {
    private const val MULTICAST_LOCK_TAG = "com.srilakshmikanthanp.clipbirdroid:mdns:browser"
    private const val TAG = "Register"
  }

  // callbacks for service discovery events.
  interface RegisterListener {
    fun onServiceUnregistered()
    fun onServiceRegistered()
    fun onServiceRegistrationFailed(errorCode: Int)
    fun onServiceUnregistrationFailed(errorCode: Int)
  }

  /**
   * Adds a listener to the browser.
   */
  fun addRegisterListener(listener: RegisterListener) {
    listeners.add(listener)
  }

  /**
   * Removes a listener from the browser.
   */
  fun removeRegisterListener(listener: RegisterListener) {
    listeners.remove(listener)
  }

  /**
   * Register the Service
   */
  fun registerService(port: Int) {
    if (this.isRegistered()) throw IllegalStateException("Service is already registered")

    // create the service info
    val serviceInfo = NsdServiceInfo()

    // set the info
    serviceInfo.serviceName = appMdnsServiceName(context)
    serviceInfo.serviceType = appMdnsServiceType()
    serviceInfo.port = port

    multicastLock.acquire()

    // register the service
    nsdManager.registerService(
      serviceInfo, PROTOCOL_DNS_SD, this
    )
  }

  /**
   * Unregister the Service
   */
  fun unRegisterService() {
    nsdManager.unregisterService(this)
    multicastLock.release()
  }

  fun reRegister(port: Int) {
    if (isRegistered()) {
      pendingRestartPort.set(port)
      unRegisterService()
      return
    } else {
      registerService(port)
    }
  }

  fun isRegistered(): Boolean {
    return isRegistered.get()
  }

  /**
   * @brief called when the unregistering service Failed
   */
  override fun onUnregistrationFailed(p0: NsdServiceInfo?, p1: Int) {
    for (listener in listeners) listener.onServiceUnregistrationFailed(p1)
  }

  /**
   * @brief called when the service is registered
   */
  override fun onServiceRegistered(p0: NsdServiceInfo?) {
    isRegistered.set(true); for (listener in listeners) listener.onServiceRegistered()
  }

  /**
   * @brief called when the service is unregistered
   */
  override fun onServiceUnregistered(p0: NsdServiceInfo?) {
    isRegistered.set(false)
    for (listener in listeners) listener.onServiceUnregistered()
    if (pendingRestartPort.get() != -1) {
      registerService(pendingRestartPort.get())
      pendingRestartPort.set(-1)
    }
  }

  /**
   * @brief called when the service registration is Failed
   */
  override fun onRegistrationFailed(p0: NsdServiceInfo?, p1: Int) {
    for (listener in listeners) listener.onServiceRegistrationFailed(p1)
  }
}
