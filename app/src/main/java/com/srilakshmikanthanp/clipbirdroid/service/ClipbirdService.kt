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
import com.srilakshmikanthanp.clipbirdroid.controller.AppController
import com.srilakshmikanthanp.clipbirdroid.common.types.Device
import com.srilakshmikanthanp.clipbirdroid.common.enums.HostType
import com.srilakshmikanthanp.clipbirdroid.handlers.WifiApStateChangeHandler
import com.srilakshmikanthanp.clipbirdroid.Clipbird
import com.srilakshmikanthanp.clipbirdroid.ui.gui.MainActivity
import com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.AcceptHandler
import com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.RejectHandler
import com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.SendHandler
import com.srilakshmikanthanp.clipbirdroid.ui.gui.notification.StatusNotification

/**
 * Service for the application
 */
class ClipbirdService : Service() {
  // Create the Status Notification instance for the service instance
  private lateinit var notification: StatusNotification

  // Notification ID for the Clipbird Foreground service notification
  private val notificationId = StatusNotification.SERVICE_ID

  // Controller foe the Whole Clipbird Designed by GRASP Pattern
  private lateinit var controller: AppController

  private val wifiApStateChangeHandler = WifiApStateChangeHandler()

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
      val server = controller.getConnectedServer()
      if (server != null) {
        resources.getString(R.string.notification_title_client, server.name)
      } else {
        resources.getString(R.string.no_connection)
      }
    } else if (controller.getHostType() == HostType.SERVER) {
      val clients = controller.getConnectedClientsList().size
      resources.getString(R.string.notification_title_server, clients)
    } else {
      throw Exception("Invalid Host Type")
    }
  }

  // Return the binder instance
  override fun onBind(p0: Intent?): IBinder? = null

  // Initialize the controller instance
  override fun onCreate() {
    // Call the super class onCreate and initialize the notification
    super.onCreate().also { notification = StatusNotification(this) }

    // Initialize the controller
    controller = (this.application as Clipbird).getController()

    // Add the Sync Request Handler
    controller.addSyncRequestHandler(controller::setClipboard)

    // Add the AuthRequest Handler
    controller.addAuthRequestHandler(this::onJoinRequest)

    // on client connected to the group change notification
    controller.addServerStatusChangedHandler { s, d ->
      this.showNotification(if(s) {
        resources.getString(R.string.notification_title_client, d.name)
      } else {
        resources.getString(R.string.no_connection)
      })
    }

    // on client connected to the group change notification
    controller.addClientListChangedHandler {
      this.showNotification(resources.getString(R.string.notification_title_server, it.size))
    }

    // on host type change
    controller.addHostTypeChangeHandler {
      this.showNotification(notificationTitle())
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
    return START_STICKY
  }

  override fun onDestroy() {
    val controller = (application as Clipbird).getController()
    if (controller.getHostType() == HostType.SERVER) {
      controller.disposeServer()
    } else if (controller.getHostType() == HostType.CLIENT) {
      controller.disposeClient()
    }
  }

  // static function to start the service
  companion object {
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
