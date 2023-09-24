package com.srilakshmikanthanp.clipbirdroid.network.service.mdns

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.PROTOCOL_DNS_SD
import android.net.nsd.NsdManager.RegistrationListener
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.srilakshmikanthanp.clipbirdroid.constant.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.constant.appMdnsServiceType

class Register(private val context: Context) : RegistrationListener {
  // NsdManager instance used to discover services of a given type.
  private val nsdManager: NsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

  // callbacks for service discovery events.
  public interface RegisterListener {
    fun onServiceRegistered()
    fun onServiceUnregistered()
  }

  // List of listeners that will be notified of browser events.
  private val listeners: MutableList<RegisterListener> = mutableListOf()

  /**
   * Adds a listener to the browser.
   */
  fun addListener(listener: RegisterListener) {
    listeners.add(listener)
  }

  /**
   * Removes a listener from the browser.
   */
  fun removeListener(listener: RegisterListener) {
    listeners.remove(listener)
  }

  /**
   * Register the Service
   */
  fun registerService(port: Int) {
    // create the service info
    val serviceInfo: NsdServiceInfo = NsdServiceInfo()

    // set the info
    serviceInfo.serviceName = appMdnsServiceName()
    serviceInfo.serviceType = appMdnsServiceType()
    serviceInfo.port = port

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
  }

  /**
   * @brief called when the unregistering service Failed
   */
  override fun onUnregistrationFailed(p0: NsdServiceInfo?, p1: Int) {
    Log.e("Unregister", "Failed to unregister service: $p0")
  }

  /**
   * @brief called when the service is registered
   */
  override fun onServiceRegistered(p0: NsdServiceInfo?) {
    for (listener in listeners) {
      listener.onServiceRegistered()
    }
  }

  /**
   * @brief called when the service is unregistered
   */
  override fun onServiceUnregistered(p0: NsdServiceInfo?) {
    for (listener in listeners) {
      listener.onServiceUnregistered()
    }
  }

  /**
   * @brief called when the service registration is Failed
   */
  override fun onRegistrationFailed(p0: NsdServiceInfo?, p1: Int) {
    Log.e("Register", "Failed to register service: $p0")
  }
}
