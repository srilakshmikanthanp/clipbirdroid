package com.srilakshmikanthanp.clipbirdroid.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Binder
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.srilakshmikanthanp.clipbirdroid.Clipbird
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.broadcast.WifiApStateChangeHandler
import com.srilakshmikanthanp.clipbirdroid.common.types.Device
import com.srilakshmikanthanp.clipbirdroid.storage.Storage
import com.srilakshmikanthanp.clipbirdroid.syncing.lan.Client
import com.srilakshmikanthanp.clipbirdroid.syncing.lan.Server
import com.srilakshmikanthanp.clipbirdroid.ui.gui.MainActivity
import com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.AcceptHandler
import com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.RejectHandler
import com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.SendHandler
import com.srilakshmikanthanp.clipbirdroid.ui.gui.notification.StatusNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ClipbirdService : Service() {
  private val serviceCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private val notificationId = StatusNotification.SERVICE_ID
  private val wifiApStateChangeHandler = WifiApStateChangeHandler()

  private val connectivityListener = object : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
      if (!clipbird.wanController.wasAbnormallyDisconnectedLastly()) return
      val device = storage.getHubHostDevice() ?: return
      if (!clipbird.wanController.isHubConnected()) clipbird.wanController.connectToHub(device)
    }
  }

  private lateinit var connectivityManager: ConnectivityManager
  private lateinit var notification: StatusNotification
  @Inject lateinit var clipbird: Clipbird
  @Inject lateinit var storage: Storage

  inner class ClipbirdBinder : Binder() {
    fun getService(): ClipbirdService = this@ClipbirdService
  }

  val binder = ClipbirdBinder()

  private fun handleClientStateChanged(client: Device, connected: Boolean) {
    val server = clipbird.lanController.getHost() ?: return
    if (!connected) return
    if (server !is Server) {
      throw RuntimeException("Host is not server")
    }
    val cert = server.getClientCertificate(client)
    storage.setClientCertificate(client.name, cert)
  }

  private fun handleServerFound(server: Device) {
    val client = clipbird.lanController.getHost() ?: return
    if (client !is Client) {
      throw RuntimeException("Host is not client")
    }
    if (client.getConnectedServer() != null) return
    if (storage.hasServerCertificate(server.name)) {
      client.connectToServerSecured(server)
    }
  }

  private fun handleServerStatusChanged(status: Boolean, srv: Device) {
    val client = clipbird.lanController.getHost() ?: return
    val clipboard = clipbird.clipboardController.getClipboard()
    if (client !is Client) {
      throw RuntimeException("Host is not client")
    }

    if (status) {
      clipboard.addClipboardChangeListener(client::synchronize)
      val cert = client.getConnectedServerCertificate()
      val name = srv.name
      storage.setServerCertificate(name, cert)
      return
    }

    clipboard.removeClipboardChangeListener(client::synchronize)

    for (s in client.getServerList()) {
      if (s != srv && storage.hasServerCertificate(s.name)) {
        return client.connectToServerSecured(s)
      }
    }
  }

  private fun onSendIntent(): PendingIntent {
    Intent(this, SendHandler::class.java).also {
      return PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
    }
  }

  private fun onTapIntent(): PendingIntent {
    Intent(this, MainActivity::class.java).also {
      return PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
    }
  }

  private fun onQuitIntent(): PendingIntent {
    val intent = Intent(this, MainActivity::class.java)
    val flags = PendingIntent.FLAG_IMMUTABLE
    intent.action = MainActivity.QUIT_ACTION
    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
    return PendingIntent.getActivity(this, 0, intent, flags)
  }

  private fun onAcceptIntent(device: Device): PendingIntent {
    val intent = Intent(this, AcceptHandler::class.java)
    val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    intent.putExtra(AcceptHandler.ACCEPT_EXTRA, device)
    return PendingIntent.getActivity(this, 0, intent, flags)
  }

  private fun onRejectIntent(device: Device): PendingIntent {
    val intent = Intent(this, RejectHandler::class.java)
    val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    intent.putExtra(RejectHandler.REJECT_EXTRA, device)
    return PendingIntent.getActivity(this, 0, intent, flags)
  }

  private fun handleAuthRequest(device: Device) {
    val peerCert = clipbird.lanController.getHostAsServerOrThrow().getUnAuthenticatedClientCertificate(device)
    val cert = storage.getClientCertificate(device.name)
    if (cert != null && cert == peerCert) {
      clipbird.lanController.onClientAuthenticated(device)
    }
    val onAccept = onAcceptIntent(device)
    val onReject = onRejectIntent(device)
    notification.showJoinRequest(device.name, onAccept, onReject)
  }

  private fun showNotification(title: String) {
    val notificationLayout = RemoteViews(packageName, R.layout.notification)

    notificationLayout.setTextViewText(R.id.notify_title, title)
    notificationLayout.setOnClickPendingIntent(R.id.notify_send, onSendIntent())

    NotificationCompat.Builder(this, notification.getChannelID())
      .setSmallIcon(R.mipmap.ic_launcher_foreground)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setStyle(NotificationCompat.DecoratedCustomViewStyle())
      .setCustomContentView(notificationLayout)
      .setContentIntent(onTapIntent())
      .addAction(
        R.mipmap.ic_launcher_foreground,
        resources.getString(R.string.quit),
        onQuitIntent()
      )
      .setOngoing(true)
      .build().also {
        startForeground(notificationId, it)
      }
  }

  fun initialize() {
    this.serviceCoroutineScope.launch {
      clipbird.lanController.syncRequestEvents.collect {
        clipbird.historyController.addHistory(it)
      }
    }

    this.serviceCoroutineScope.launch {
      clipbird.wanController.syncRequestEvents.collect {
        clipbird.historyController.addHistory(it)
      }
    }

    this.serviceCoroutineScope.launch {
      clipbird.historyController.clipboard.collect {
        clipbird.clipboardController.getClipboard().setClipboardContent(it)
      }
    }

    this.serviceCoroutineScope.launch {
      clipbird.lanController.authRequestEvents.collect {
        handleAuthRequest(it)
      }
    }

    this.serviceCoroutineScope.launch {
      clipbird.lanController.serverFoundEvents.collect {
        handleServerFound(it)
      }
    }

    this.serviceCoroutineScope.launch {
      clipbird.lanController.serverStatusEvents.collect { (status, device) ->
        handleServerStatusChanged(status, device)
      }
    }

    this.serviceCoroutineScope.launch {
      clipbird.lanController.clientStateEvents.collect { (client, connected) ->
        handleClientStateChanged(client, connected)
      }
    }

    this.serviceCoroutineScope.launch {
      clipbird.lanController.syncRequestEvents.collect {
        clipbird.clipboardController.getClipboard().setClipboardContent(it)
        clipbird.historyController.addHistory(it)
      }
    }

    this.serviceCoroutineScope.launch {
      clipbird.clipboardController.clipboardChangeEvents.collect {
        clipbird.historyController.addHistory(it)
        clipbird.lanController.synchronize(it)
        clipbird.wanController.synchronize(it)
      }
    }

    if (storage.getHostIsLastlyServer()) {
      clipbird.lanController.setAsServer()
    } else {
      clipbird.lanController.setAsClient()
    }

    val hostHubDevice = storage.getHubHostDevice()
    if (hostHubDevice != null && storage.getIsLastlyConnectedToHub()) {
      clipbird.wanController.connectToHub(hostHubDevice)
    }
  }

  override fun onCreate() {
    super.onCreate()
    this.connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    this.notification = StatusNotification(this)
    val filter = IntentFilter(WifiApStateChangeHandler.ACTION_WIFI_AP_STATE_CHANGED)
    this.registerReceiver(wifiApStateChangeHandler, filter)
    this.initialize()
    val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
    connectivityManager.registerNetworkCallback(request, connectivityListener)
    this.showNotification(resources.getString(R.string.notification_title))
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent?.getBooleanExtra(HOTSPOT_ENABLED, false) == true) {
      (clipbird.lanController.getHost() as? Client)?.restartBrowsing()
    }
    return START_STICKY
  }

  override fun onDestroy() {
    super.onDestroy()
    connectivityManager.unregisterNetworkCallback(connectivityListener)
    this.unregisterReceiver(wifiApStateChangeHandler)
    this.serviceCoroutineScope.cancel()
  }

  override fun onBind(intent: Intent?): IBinder = binder

  companion object {
    const val HOTSPOT_ENABLED = "com.srilakshmikanthanp.clipbirdroid.service.ClipbirdService.HOTSPOT_ENABLED"

    fun start(context: Context) {
      Intent(context, ClipbirdService::class.java).also {
        context.startForegroundService(it)
      }
    }

    fun stop(context: Context) {
      Intent(context, ClipbirdService::class.java).also {
        context.stopService(it)
      }
    }
  }
}
