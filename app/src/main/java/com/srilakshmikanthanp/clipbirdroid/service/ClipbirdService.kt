package com.srilakshmikanthanp.clipbirdroid.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.common.enums.HostType
import com.srilakshmikanthanp.clipbirdroid.common.functions.generateX509Certificate
import com.srilakshmikanthanp.clipbirdroid.common.types.Device
import com.srilakshmikanthanp.clipbirdroid.constants.appCertExpiryInterval
import com.srilakshmikanthanp.clipbirdroid.constants.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.controller.AppController
import com.srilakshmikanthanp.clipbirdroid.handlers.WifiApStateChangeHandler
import com.srilakshmikanthanp.clipbirdroid.store.Storage
import com.srilakshmikanthanp.clipbirdroid.ui.gui.MainActivity
import com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.AcceptHandler
import com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.RejectHandler
import com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.SendHandler
import com.srilakshmikanthanp.clipbirdroid.ui.gui.notification.StatusNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import java.security.PrivateKey
import java.security.cert.X509Certificate

/**
 * Service for the application
 */
class ClipbirdService : Service() {
  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

  // Create the Status Notification instance for the service instance
  private lateinit var notification: StatusNotification

  // Notification ID for the Clipbird Foreground service notification
  private val notificationId = StatusNotification.SERVICE_ID

  // Controller foe the Whole Clipbird Designed by GRASP Pattern
  private lateinit var controller: AppController

  private val wifiApStateChangeHandler = WifiApStateChangeHandler()

  // Function used to get the Private Key and the Certificate New
  private fun getNewSslConfig(): Pair<PrivateKey, X509Certificate> {
    val sslConfig = generateX509Certificate(this)
    val store = Storage.getInstance(this)
    store.setHostKey(sslConfig.first)
    store.setHostCert(sslConfig.second)
    return sslConfig
  }

  // Function used to get the Private Key and the Certificate Old
  private fun getOldSslConfig(): Pair<PrivateKey, X509Certificate> {
    // Get the Certificate Details
    val store = Storage.getInstance(this)
    val key = store.getHostKey()!!
    val cert = store.getHostCert()!!

    // Get the Required parameters
    val x500Name = JcaX509CertificateHolder(cert).subject
    val cn = x500Name.getRDNs(BCStyle.CN)[0]
    val name = IETFUtils.valueToString(cn.first.value)

    // device name
    val deviceName = appMdnsServiceName(this)

    // check the name is same
    if (name != deviceName) {
      return getNewSslConfig()
    }

    // is certificate is to expiry in two months
    if (cert.notAfter.time - System.currentTimeMillis() < appCertExpiryInterval()) {
      val sslConfig = generateX509Certificate(this)
      store.setHostKey(sslConfig.first)
      store.setHostCert(sslConfig.second)
      return sslConfig
    }

    // done return
    return Pair(key, cert)
  }

  // Function used to get the the Private Key and the Certificate
  private fun getSslConfig(): Pair<PrivateKey, X509Certificate> {
    // Get the Storage instance for the application
    val store = Storage.getInstance(this)

    // Check the Host key and cert is available
    val config = if (store.hasHostKey() && store.hasHostCert()) {
      getOldSslConfig()
    } else {
      getNewSslConfig()
    }

    // return the config
    return config
  }

  // Function used to get the Pending intent for onSend
  private fun onSendIntent(): PendingIntent {
    Intent(this, SendHandler::class.java).also {
      return PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
    }
  }

  // Function used to get the Pending intent for onTap
  private fun onTapIntent(): PendingIntent {
    Intent(this, MainActivity::class.java).also {
      return PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
    }
  }

  // Function used to get the Pending intent for onQuit
  private fun onQuitIntent(): PendingIntent {
    val intent = Intent(this, MainActivity::class.java)
    val flags = PendingIntent.FLAG_IMMUTABLE
    intent.action = MainActivity.QUIT_ACTION
    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
    return PendingIntent.getActivity(this, 0, intent, flags)
  }

  // Function used to get the Pending intent for onAccept
  private fun onAcceptIntent(device: Device): PendingIntent {
    val intent = Intent(this, AcceptHandler::class.java)
    val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    intent.putExtra(AcceptHandler.ACCEPT_EXTRA, device)
    return PendingIntent.getActivity(this, 0, intent, flags)
  }

  // Function used to get the Pending intent for onReject
  private fun onRejectIntent(device: Device): PendingIntent {
    val intent = Intent(this, RejectHandler::class.java)
    val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    intent.putExtra(RejectHandler.REJECT_EXTRA, device)
    return PendingIntent.getActivity(this, 0, intent, flags)
  }

  // Called when an client wants to join the group
  private fun onJoinRequest(device: Device) {
    notification.showJoinRequest(device.name, onAcceptIntent(device), onRejectIntent(device))
  }

  // show the notification
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
      .addAction(R.mipmap.ic_launcher_foreground, resources.getString(R.string.quit), onQuitIntent())
      .setOngoing(true)
      .build().also {
        startForeground(notificationId, it)
      }
  }

  // infer the title
  private fun notificationTitle(): String {
    return if (controller.getHostType() == HostType.CLIENT) {
      controller.getConnectedServer()?.name?.let { resources.getString(R.string.notification_title_client, it) } ?: resources.getString(R.string.no_connection)
    } else if (controller.getHostType() == HostType.SERVER) {
      resources.getString(R.string.notification_title_server, controller.getConnectedClientsList().size)
    } else {
      throw Exception("Invalid Host Type")
    }
  }

  // Initialize the controller instance
  override fun onCreate() {
    // Call the super class onCreate and initialize the notification
    super.onCreate().also { notification = StatusNotification(this) }

    // Initialize the controller
    controller = AppController(getSslConfig(), this)

    if (controller.isLastlyHostIsServer()) {
      controller.setCurrentHostAsServer()
    } else {
      controller.setCurrentHostAsClient()
    }

    // Add the Sync Request Handler
    this.serviceScope.launch {
      controller.syncRequests.collect { controller.setClipboard(it) }
    }

    // Add the AuthRequest Handler
    this.serviceScope.launch {
      controller.authRequest.collect { onJoinRequest(it) }
    }

    // on client connected to the group change notification
    this.serviceScope.launch {
      controller.serverStatus.collect { (s, d) ->
        this@ClipbirdService.showNotification(if (s) {
          resources.getString(R.string.notification_title_client, d.name)
        } else {
          resources.getString(R.string.no_connection)
        })
      }
    }

    // on client connected to the group change notification
    this.serviceScope.launch {
      controller.clients.collect { clients ->
        this@ClipbirdService.showNotification(
          resources.getString(R.string.notification_title_server, clients.size)
        )
      }
    }

    // on host type change
    this.serviceScope.launch {
      controller.hostTypeChangeEvent.collect {
        this@ClipbirdService.showNotification(notificationTitle())
      }
    }

    this.registerReceiver(
      wifiApStateChangeHandler,
      IntentFilter(WifiApStateChangeHandler.ACTION_WIFI_AP_STATE_CHANGED)
    )

    // show the notification
    this.showNotification(this.notificationTitle())
  }

  // On start command of the service
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent?.getBooleanExtra(HOTSPOT_ENABLED, false) == true) {
      if (controller.getHostType() == HostType.CLIENT) {
        controller.restartBrowsing()
      }
    }
    return START_STICKY
  }

  override fun onDestroy() {
    if (controller.getHostType() == HostType.SERVER) {
      controller.disposeServer()
    } else if (controller.getHostType() == HostType.CLIENT) {
      controller.disposeClient()
    }
    this.serviceScope.cancel()
    this.controller.close()
  }

  fun getController(): AppController = controller

  inner class ClipbirdBinder : android.os.Binder() {
    fun getService(): ClipbirdService = this@ClipbirdService
  }

  val binder = ClipbirdBinder()

  override fun onBind(intent: Intent?): IBinder = binder

  // static function to start the service
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
