package com.srilakshmikanthanp.clipbirdroid.ui.gui.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.controller.AppController
import com.srilakshmikanthanp.clipbirdroid.store.Storage
import com.srilakshmikanthanp.clipbirdroid.types.device.Device
import com.srilakshmikanthanp.clipbirdroid.ui.gui.MainActivity
import com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.AcceptHandler
import com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.QuitHandler
import com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.RejectHandler
import com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.SendHandler
import com.srilakshmikanthanp.clipbirdroid.ui.gui.notifications.StatusNotification
import com.srilakshmikanthanp.clipbirdroid.utility.functions.generateX509Certificate
import java.security.PrivateKey
import java.security.cert.X509Certificate

/**
 * Service for the application
 */
class ClipbirdService : Service() {
  // Notification ID for the CLipbird Foreground service notification
  private val NOTIFICATION_ID = StatusNotification.SERVICE_ID

  // Controller foe the Whole Application Designed by GRASP Pattern
  private lateinit var controller: AppController

  // Create the Status Notification instance for the service instance
  private lateinit var notification: StatusNotification

  // Binder instance
  private val binder = ServiceBinder()

  // Binder for the service that returns the service instance
  inner class ServiceBinder : Binder() {
    fun getService(): ClipbirdService = this@ClipbirdService
  }

  // Function used to get the Private Key and the Certificate New
  private fun getNewSslConfig(): Pair<PrivateKey, X509Certificate> {
    val sslConfig =  generateX509Certificate(this)
    val store = Storage.getInstance(this)
    store.setHostKey(sslConfig.first)
    store.setHostCert(sslConfig.second)
    return sslConfig
  }

  // Function used to get the Private Key and the Certificate Old
  private fun getOldSslConfig(): Pair<PrivateKey, X509Certificate> {
    val store = Storage.getInstance(this)
    return Pair(store.getHostKey()!!, store.getHostCert()!!)
  }

  // Function used to get the the Private Key and the Certificate
  private fun getSslConfig(): Pair<PrivateKey, X509Certificate> {
    // Get the Storage instance
    val store = Storage.getInstance(this)

    // Check the Host key and cert is available
    if (store.hasHostKey() && store.hasHostCert()) {
      return getOldSslConfig()
    }

    // If not available generate new
    return getNewSslConfig()
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
    Intent(this, QuitHandler::class.java).also {
      return PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
    }
  }

  // Function used to get the Pending intent for onAccept
  private fun onAcceptIntent(device: Device): PendingIntent {
    val intent: Intent = Intent(this, AcceptHandler::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP;
    intent.putExtra(AcceptHandler.ACCEPT_EXTRA, device)
    return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
  }

  // Function used to get the Pending intent for onReject
  private fun onRejectIntent(device: Device): PendingIntent {
    val intent: Intent = Intent(this, RejectHandler::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP;
    intent.putExtra(RejectHandler.REJECT_EXTRA, device)
    return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
  }

  // Called when an client wants to join the group
  private fun onJoinRequest(device: Device) {
    notification.showJoinRequest(device.name, onAcceptIntent(device), onRejectIntent(device))
  }

  // Initialize the controller instance
  override fun onCreate() {
    super.onCreate().also {
      controller    =   AppController(getSslConfig(), this)
      notification  =   StatusNotification(this)
    }

    // Add the Sync Request Handler
    controller.addSyncRequestHandler(controller::setClipboard)

    // Add the AuthRequest Handler
    controller.addAuthRequestHandler(this::onJoinRequest)

    // initialize the controller
    if (controller.isLastlyHostIsServer()) {
      controller.setCurrentHostAsServer()
    } else {
      controller.setCurrentHostAsClient()
    }

    // Create the notification
    NotificationCompat.Builder(this, notification.getChannelID())
      .setSmallIcon(R.mipmap.ic_launcher_foreground)
      .setContentTitle("Clipbird Service")
      .setContentText("Send latest clipboard content to other devices")
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setContentIntent(onTapIntent())
      .setOngoing(true)
      .addAction(0, "Send", onSendIntent())
      .addAction(0, "Quit", onQuitIntent())
      .build().also {
        startForeground(NOTIFICATION_ID, it)
      }
  }

  // Return the binder instance
  override fun onBind(p0: Intent?): IBinder {
    return binder
  }

  // On start command of the service
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    // Return code for the service
    return START_REDELIVER_INTENT
  }

  // Get the Controller instance of the service
  fun getController(): AppController = controller
}
