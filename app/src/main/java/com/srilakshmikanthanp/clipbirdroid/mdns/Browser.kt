package com.srilakshmikanthanp.clipbirdroid.mdns

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.DiscoveryListener
import android.net.nsd.NsdManager.ResolveListener
import android.net.nsd.NsdManager.ServiceInfoCallback
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.srilakshmikanthanp.clipbirdroid.constant.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.constant.appMdnsServiceType
import com.srilakshmikanthanp.clipbirdroid.common.types.Device
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Browser that allows to discover services of a given type.
 */
class Browser(private val context: Context) : DiscoveryListener {
  // NsdManager instance used to discover services of a given type.
  private val nsdManager: NsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

  // Executor instance for running tasks in the background.
  private val executor: Executor = Executors.newSingleThreadExecutor()

  // List of listeners that will be notified of browser events.
  private val listeners: MutableList<BrowserListener> = mutableListOf()

  // List of Map of service name and ip
  private val serviceMap: MutableMap<String, Pair<InetAddress, Int>> = mutableMapOf()

  /**
   * Removes a listener from the browser.
   */
  fun removeListener(listener: BrowserListener) {
    listeners.remove(listener)
  }

  /**
   * Adds a listener to the browser.
   */
  fun addListener(listener: BrowserListener) {
    listeners.add(listener)
  }

  /**
   * Notifies the listeners that a service has been removed.
   */
  private fun notifyServiceRemoved(device: Device) {
    for (l in listeners) l.onServiceRemoved(device)
  }

  /**
   * Notifies the listeners that a service has been added.
   */
  private fun notifyServiceAdded(device: Device) {
    for (l in listeners) l.onServiceAdded(device)
  }

  /**
   * service Resolved
   */
  private fun serviceResolved(info: NsdServiceInfo) {
    // notify the listeners
    val serviceName: String = info.serviceName
    val port: Int = info.port
    val host: InetAddress = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      info.hostAddresses.find { it is Inet4Address }
    } else {
      info.host
    } ?: return

    if (serviceMap.contains(info.serviceName)) {
      return
    }

    // Check the ip is my device ip
    val interfaces = NetworkInterface.getNetworkInterfaces()

    // Iterate all the network interfaces
    while (interfaces.hasMoreElements()) {
      val addresses = interfaces.nextElement().inetAddresses
      if (addresses.asSequence().any { it == host }) return
    }

    // Add to the service map
    serviceMap[serviceName] = host to port

    // notify the listeners
    notifyServiceAdded(Device(host, port, serviceName))
  }

  /**
   * service Lost Call Back
   */
  private fun serviceLost(info: NsdServiceInfo) {
    // ignore the service of this app
    if (info.serviceName == appMdnsServiceName(context)) return

    // get the ip for the service
    val serviceName: String = info.serviceName
    val ip = serviceMap[serviceName]?.first ?: return
    val port = serviceMap[serviceName]?.second ?: return

    // remove from map
    serviceMap.remove(serviceName)

    // notify the listeners
    notifyServiceRemoved(Device(ip, port, serviceName))
  }

  // Resolve Listener for Network Service Discovery API 28
  private inner class ResolverOld : ResolveListener {
    override fun onResolveFailed(p0: NsdServiceInfo?, p1: Int) {
      Log.e(TAG, "Failed to resolve service: $p0")
    }

    override fun onServiceResolved(info: NsdServiceInfo) {
      serviceResolved(info)
    }
  }

  // Resolve Listener for Network Service Discovery API 34
  @RequiresApi(34)
  private inner class ResolverNew : ServiceInfoCallback {
    override fun onServiceInfoCallbackRegistrationFailed(p0: Int) {
      Log.e(TAG, "Failed to resolve service: $p0")
    }

    override fun onServiceUpdated(info: NsdServiceInfo) {
      serviceResolved(info)
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
    private const val TAG = "Browser"
  }

  // callbacks for service discovery events.
  interface BrowserListener {
    fun onServiceRemoved(device: Device)
    fun onServiceAdded(device: Device)
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
    try {
      nsdManager.stopServiceDiscovery(this)
    } catch (e: IllegalStateException) {
      Log.w(TAG, e.message, e)
    }
  }

  /**
   * Called when a service is lost.
   */
  override fun onServiceLost(info: NsdServiceInfo) {
    serviceLost(info)
  }

  /**
   * Called when a service is found.
   */
  override fun onServiceFound(info: NsdServiceInfo) {
    if (info.serviceName == appMdnsServiceName(context)) {
      return
    } else if (Build.VERSION.SDK_INT >= 34) {
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
