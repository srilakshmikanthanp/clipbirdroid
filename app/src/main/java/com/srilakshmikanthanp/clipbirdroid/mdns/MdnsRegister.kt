package com.srilakshmikanthanp.clipbirdroid.mdns

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.RegistrationListener
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.MulticastLock
import com.srilakshmikanthanp.clipbirdroid.constant.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.constant.appMdnsServiceType
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class MdnsRegister(private val context: Context) : Register {
  private val nsdManager: NsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

  private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

  private val listeners: MutableList<RegisterListener> = mutableListOf()

  private var listener = AtomicReference<RegisterRegistrationListener?>(null)

  private var pendingRestartPort: AtomicInteger = AtomicInteger(-1)

  private val multicastLock: MulticastLock = wifiManager.createMulticastLock(MULTICAST_LOCK_TAG)

  private inner class RegisterRegistrationListener : RegistrationListener {
    override fun onUnregistrationFailed(p0: NsdServiceInfo?, p1: Int) {
      for (listener in listeners) {
        listener.onServiceUnregistrationFailed(p1)
      }
    }

    override fun onServiceRegistered(p0: NsdServiceInfo?) {
      for (listener in listeners) {
        listener.onServiceRegistered()
      }
    }

    override fun onServiceUnregistered(p0: NsdServiceInfo?) {
      multicastLock.release()
      listener.set(null)
      for (listener in listeners) {
        listener.onServiceUnregistered()
      }
      if (pendingRestartPort.get() != -1) {
        registerService(pendingRestartPort.get())
        pendingRestartPort.set(-1)
      }
    }

    override fun onRegistrationFailed(p0: NsdServiceInfo?, p1: Int) {
      for (listener in listeners) {
        listener.onServiceRegistrationFailed(p1)
      }
    }
  }

  // TAG for logging.
  companion object {
    private const val MULTICAST_LOCK_TAG = "com.srilakshmikanthanp.clipbirdroid:mdns:browser"
    private const val TAG = "Register"
  }

  /**
   * Adds a listener to the browser.
   */
  override fun addRegisterListener(listener: RegisterListener) {
    listeners.add(listener)
  }

  /**
   * Removes a listener from the browser.
   */
  override fun removeRegisterListener(listener: RegisterListener) {
    listeners.remove(listener)
  }

  /**
   * Register the Service
   */
  override fun registerService(port: Int) {
    if (this.isRegistered()) throw IllegalStateException("Service is already registered")

    this.listener.set(RegisterRegistrationListener())

    // create the service info
    val serviceInfo = NsdServiceInfo()

    // set the info
    serviceInfo.serviceName = appMdnsServiceName(context)
    serviceInfo.serviceType = appMdnsServiceType()
    serviceInfo.port = port

    multicastLock.acquire()

    // register the service
    nsdManager.registerService(
      serviceInfo, NsdManager.PROTOCOL_DNS_SD, this.listener.get()
    )
  }

  /**
   * Unregister the Service
   */
  override fun unRegisterService() {
    nsdManager.unregisterService(this.listener.get())
  }

  override fun reRegister(port: Int) {
    if (isRegistered()) {
      pendingRestartPort.set(port)
      unRegisterService()
      return
    } else {
      registerService(port)
    }
  }

  override fun isRegistered(): Boolean {
    return listener.get() != null
  }
}
