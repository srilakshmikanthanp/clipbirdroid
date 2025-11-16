package com.srilakshmikanthanp.clipbirdroid.syncing.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.RegistrationListener
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.MulticastLock
import com.srilakshmikanthanp.clipbirdroid.constants.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.constants.appMdnsServiceType
import java.util.concurrent.atomic.AtomicReference

class MdnsNetRegister(private val context: Context) : NetRegister {
  private val nsdManager: NsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
  private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
  private val listeners: MutableList<NetRegisterListener> = mutableListOf()
  private var listener = AtomicReference<RegisterRegistrationListener?>(null)
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
    }

    override fun onRegistrationFailed(p0: NsdServiceInfo?, p1: Int) {
      for (listener in listeners) {
        listener.onServiceRegistrationFailed(p1)
      }
    }
  }

  companion object {
    private const val MULTICAST_LOCK_TAG = "com.srilakshmikanthanp.clipbirdroid:mdns:browser"
  }

  override fun addRegisterListener(listener: NetRegisterListener) {
    listeners.add(listener)
  }

  override fun removeRegisterListener(listener: NetRegisterListener) {
    listeners.remove(listener)
  }

  override fun registerService(port: Int) {
    if (listener.get() != null) throw IllegalStateException("Service is already registered")
    this.listener.set(RegisterRegistrationListener())
    val serviceInfo = NsdServiceInfo()
    serviceInfo.serviceName = appMdnsServiceName(context)
    serviceInfo.serviceType = appMdnsServiceType()
    serviceInfo.port = port
    multicastLock.acquire()
    nsdManager.registerService(
      serviceInfo, NsdManager.PROTOCOL_DNS_SD, this.listener.get()
    )
  }

  override fun unregisterService() {
    if (listener.get() == null) throw IllegalStateException("Service is not registered")
    nsdManager.unregisterService(this.listener.get())
  }
}
