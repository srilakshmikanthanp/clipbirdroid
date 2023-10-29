package com.srilakshmikanthanp.clipbirdroid.network.service

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.DiscoveryListener
import android.net.nsd.NsdManager.ResolveListener
import android.net.nsd.NsdManager.ServiceInfoCallback
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.annotation.RequiresApi
import com.srilakshmikanthanp.clipbirdroid.constant.appMdnsServiceType
import com.srilakshmikanthanp.clipbirdroid.types.device.Device
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Browser that allows to discover services of a given type.
 */
class Browser(context: Context) : DiscoveryListener {
  // NsdManager instance used to discover services of a given type.
  private val nsdManager: NsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

  // List of listeners that will be notified of browser events.
  private val listeners: MutableList<BrowserListener> = mutableListOf()

  // Executor instance for running tasks in the background.
  private val executor: Executor = Executors.newSingleThreadExecutor()

  // Resolve Listener for Network Service Discovery API 28
  private inner class ResolverOld: ResolveListener {
    override fun onServiceResolved(info: NsdServiceInfo) {
      for (l in listeners) l.onServiceAdded(Device(info.host, info.port, info.serviceName))
    }

    override fun onResolveFailed(p0: NsdServiceInfo?, p1: Int) {
      Log.e(TAG, "Failed to resolve service: $p0")
    }
  }

  // Resolve Listener for Network Service Discovery API 34
  @RequiresApi(34)
  private inner class ResolverNew: ServiceInfoCallback {
    override fun onServiceInfoCallbackRegistrationFailed(p0: Int) {
      Log.e(TAG, "Failed to resolve service: $p0")
    }

    override fun onServiceUpdated(p0: NsdServiceInfo) {
      for (l in listeners) l.onServiceAdded(Device(p0.hostAddresses.first(), p0.port, p0.serviceName))
    }

    override fun onServiceLost() {
      Log.e(TAG, "Failed to resolve service")
    }

    override fun onServiceInfoCallbackUnregistered() {
      Log.d(TAG, "onServiceInfoCallbackUnregistered")
    }
  }

  // TAG for logging.
  companion object {
    private val TAG = "Browser"
  }

  // callbacks for service discovery events.
  interface BrowserListener {
    fun onServiceRemoved(device: Device)
    fun onServiceAdded(device: Device)
  }

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
    for (l in listeners) l.onServiceRemoved(Device(info.host, info.port, info.serviceName))
  }

  /**
   * Called when a service is found.
   */
  override fun onServiceFound(info: NsdServiceInfo) {
    if (android.os.Build.VERSION.SDK_INT >= 34) {
      nsdManager.registerServiceInfoCallback(info, executor, ResolverNew())
    } else {
      nsdManager.resolveService(info, ResolverOld())
    }
  }

  /**
   * Called when a discovery is started. [Not Used]
   */
  override fun onDiscoveryStarted(p0: String?) {
    Log.d(TAG, "Discovery started: $p0")
  }

  /**
   * Called when a discovery is stopped. [Not Used]
   */
  override fun onDiscoveryStopped(p0: String?) {
    Log.d(TAG, "Discovery stopped: $p0")
  }

  /**
   * Called when a start discovery fails. [Not Used]
   */
  override fun onStartDiscoveryFailed(p0: String?, p1: Int) {
    Log.e(TAG, "Failed to start discovery: $p0")
  }

  /**
   * Called when a stop discovery fails. [Not Used]
   */
  override fun onStopDiscoveryFailed(p0: String?, p1: Int) {
    Log.e(TAG, "Failed to stop discovery: $p0")
  }
}
