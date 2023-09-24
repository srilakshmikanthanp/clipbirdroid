package com.srilakshmikanthanp.clipbirdroid.network.service.mdns

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.DiscoveryListener
import android.net.nsd.NsdManager.ResolveListener
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.srilakshmikanthanp.clipbirdroid.constant.appMdnsServiceType
import com.srilakshmikanthanp.clipbirdroid.types.device.Device

/**
 * Browser that allows to discover services of a given type.
 */
class Browser(private val context: Context) : ResolveListener, DiscoveryListener {
  // NsdManager instance used to discover services of a given type.
  private val nsdManager: NsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

  // callbacks for service discovery events.
  public interface BrowserListener {
    fun onServiceRemoved(device: Device)
    fun onServiceAdded(device: Device)
  }

  // List of listeners that will be notified of browser events.
  private val listeners: MutableList<BrowserListener> = mutableListOf()

  /**
   * Adds a listener to the browser.
   */
  fun addListener(listener: BrowserListener) {
    listeners.add(listener)
  }

  /**
   * Removes a listener from the browser.
   */
  fun removeListener(listener: BrowserListener) {
    listeners.remove(listener)
  }

  /**
   * Starts the browser.
   */
  fun start() {
    nsdManager.discoverServices(appMdnsServiceType(), NsdManager.PROTOCOL_DNS_SD, this)
  }

  /**
   * Stops the browser.
   */
  fun stop() {
    nsdManager.stopServiceDiscovery(this)
  }

  /**
   * Called when a service is lost.
   */
  override fun onServiceLost(info: NsdServiceInfo) {
    for (listener in listeners) {
      listener.onServiceRemoved(Device(info.host, info.port, info.serviceName))
    }
  }

  /**
   * Called when a service is found.
   */
  override fun onServiceFound(info: NsdServiceInfo?) {
    nsdManager.resolveService(info, this)
  }

  /**
   * Called when a service is resolved.
   */
  override fun onServiceResolved(info: NsdServiceInfo) {
    for (listener in listeners) {
      listener.onServiceAdded(Device(info.host, info.port, info.serviceName))
    }
  }

  /**
   * Called when a resolve fails. [Not Used]
   */
  override fun onResolveFailed(p0: NsdServiceInfo?, p1: Int) {
    Log.e("Browser", "Failed to resolve service: $p0")
  }

  /**
   * Called when a discovery is started. [Not Used]
   */
  override fun onDiscoveryStarted(p0: String?) {
    Log.d("Browser", "Discovery started: $p0")
  }

  /**
   * Called when a discovery is stopped. [Not Used]
   */
  override fun onDiscoveryStopped(p0: String?) {
    Log.d("Browser", "Discovery stopped: $p0")
  }

  /**
   * Called when a start discovery fails. [Not Used]
   */
  override fun onStartDiscoveryFailed(p0: String?, p1: Int) {
    Log.e("Browser", "Failed to start discovery: $p0")
  }

  /**
   * Called when a stop discovery fails. [Not Used]
   */
  override fun onStopDiscoveryFailed(p0: String?, p1: Int) {
    Log.e("Browser", "Failed to stop discovery: $p0")
  }
}
