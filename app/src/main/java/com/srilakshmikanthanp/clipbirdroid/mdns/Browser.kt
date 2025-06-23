package com.srilakshmikanthanp.clipbirdroid.mdns

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.DiscoveryListener
import android.net.nsd.NsdManager.ResolveListener
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.MulticastLock
import android.os.Build
import android.util.Log
import com.srilakshmikanthanp.clipbirdroid.common.types.Device
import com.srilakshmikanthanp.clipbirdroid.constant.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.constant.appMdnsServiceType
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class Browser(private val context: Context) {
  private val nsdManager: NsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

  private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

  private val listeners: MutableList<BrowserListener> = mutableListOf()

  private val serviceMap: MutableMap<String, Pair<InetAddress, Int>> = mutableMapOf()

  private val listener = AtomicReference<DiscoveryListener?>(null)

  private var isPendingRestart: AtomicBoolean = AtomicBoolean(false)

  private val multicastLock: MulticastLock = wifiManager.createMulticastLock(MULTICAST_LOCK_TAG)

  private val serviceResolveQueue = ServiceResolveQueue(nsdManager)

  private inner class BrowserDiscoveryListener: DiscoveryListener {
    override fun onServiceFound(info: NsdServiceInfo) {
      if (info.serviceName == appMdnsServiceName(context)) {
        return
      } else {
        serviceResolveQueue.enqueue(info, ResolverOld())
      }
    }

    override fun onServiceLost(info: NsdServiceInfo) {
      serviceLost(info)
    }

    override fun onDiscoveryStarted(p0: String?) {
      multicastLock.acquire()
      for (l in listeners) {
        l.onBrowsingStatusChanged(true)
      }
    }

    override fun onDiscoveryStopped(p0: String?) {
      multicastLock.release()
      serviceMap.clear()
      listener.set(null)
      for (l in listeners) {
        l.onBrowsingStatusChanged(false)
      }
      if (isPendingRestart.get()) {
        isPendingRestart.set(false)
        start()
      }
    }

    override fun onStartDiscoveryFailed(p0: String?, p1: Int) {
      for (l in listeners) {
        l.onStartBrowsingFailed(p1)
      }
    }

    override fun onStopDiscoveryFailed(p0: String?, p1: Int) {
      for (l in listeners) {
        l.onStopBrowsingFailed(p1)
      }
    }
  }

  private inner class ResolverOld : ResolveListener {
    override fun onResolveFailed(p0: NsdServiceInfo?, p1: Int) {
      Log.e(TAG, "Failed to resolve service: $p0")
    }

    override fun onServiceResolved(info: NsdServiceInfo) {
      serviceResolved(info)
    }
  }

  fun removeListener(listener: BrowserListener) {
    listeners.remove(listener)
  }

  fun addListener(listener: BrowserListener) {
    listeners.add(listener)
  }

  private fun notifyServiceRemoved(device: Device) {
    for (l in listeners) l.onServiceRemoved(device)
  }

  private fun notifyServiceAdded(device: Device) {
    for (l in listeners) l.onServiceAdded(device)
  }

  private fun serviceResolved(info: NsdServiceInfo) {
    // notify the listeners
    val serviceName: String = info.serviceName
    val port: Int = info.port
    val host: InetAddress = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      info.hostAddresses.first()
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

  // TAG for logging.
  companion object {
    private const val MULTICAST_LOCK_TAG = "com.srilakshmikanthanp.clipbirdroid:mdns:browser"
    private const val TAG = "Browser"
  }

  // callbacks for service discovery events.
  interface BrowserListener {
    fun onBrowsingStatusChanged(isBrowsing: Boolean)
    fun onServiceRemoved(device: Device)
    fun onServiceAdded(device: Device)
    fun onStartBrowsingFailed(errorCode: Int)
    fun onStopBrowsingFailed(errorCode: Int)
  }

  fun isBrowsing(): Boolean {
    return listener.get() != null
  }

  fun start() {
    if (isBrowsing()) throw IllegalStateException("Browser is already started")
    this.listener.set(BrowserDiscoveryListener())
    nsdManager.discoverServices(
      appMdnsServiceType(),
      NsdManager.PROTOCOL_DNS_SD,
      this.listener.get()
    )
  }

  fun stop() {
    nsdManager.stopServiceDiscovery(this.listener.get())
  }

  fun restart() {
    if (isBrowsing()) {
      isPendingRestart.set(true)
      stop()
    } else {
      start()
    }
  }
}

@Deprecated("Using resolveService which is deprecated in android")
class ServiceResolveQueue(private val nsdManager: NsdManager) {
  private val resolveQueue: LinkedList<Pair<NsdServiceInfo, ResolveListener>> = LinkedList()
  private val lock: Any = Any()

  private inner class ResolveListenerWrapper(private val listener: ResolveListener) : ResolveListener {
    override fun onResolveFailed(p0: NsdServiceInfo?, p1: Int) {
      listener.onResolveFailed(p0, p1)
      postResolve()
    }

    override fun onServiceResolved(info: NsdServiceInfo) {
      listener.onServiceResolved(info)
      postResolve()
    }
  }

  private fun resolveNext() {
    val pair = resolveQueue.peek() ?: return
    val info = pair.first
    val listener = pair.second
    nsdManager.resolveService(info, listener)
  }

  private fun postResolve() {
    synchronized(lock) {
      resolveQueue.pop()
      resolveNext()
    }
  }

  fun enqueue(info: NsdServiceInfo, listener: ResolveListener) {
    synchronized(lock) {
      resolveQueue.add(Pair(info, ResolveListenerWrapper(listener)))
      if (resolveQueue.size == 1) {
        resolveNext()
      }
    }
  }
}
