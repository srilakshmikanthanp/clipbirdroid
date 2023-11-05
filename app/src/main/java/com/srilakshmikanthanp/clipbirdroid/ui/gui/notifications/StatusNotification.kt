package com.srilakshmikanthanp.clipbirdroid.ui.gui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.srilakshmikanthanp.clipbirdroid.R

/**
 * A Notification Channel that has the following Notifications:
 *
 * 1) -> present all the time when the app is running.
 * When user clicks the Notification, the latest clipboard content
 * will be send to other devices in the group. Finally it has an
 * button to quit the app.
 *
 * 2) -> when a device wants to connect to the group, this notification
 * will be shown to the user. it has two buttons, one for accept and
 * another for reject.
 */
class StatusNotification(private val context: Context, onTap: PendingIntent, onSend: PendingIntent, onQuit: PendingIntent) {
  // Notification Manager instance
  private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  // Notification channel constants
  private val CHANNEL_DESC = "This is the notification for Clipbird Service"
  private val CHANNEL_ID   = "StatusNotification"
  private val CHANNEL_NAME = "Clipbird Service"
  private val IMPORTANCE   = NotificationManager.IMPORTANCE_DEFAULT
  private val SERVICE_ID   = 1
  private val REQUEST_ID   = 2

  /**
   * Initializer that creates Channel and Notification
   */
  init {
    // Create the NotificationChannel for API 26+
    NotificationChannel(CHANNEL_ID, CHANNEL_NAME, IMPORTANCE).apply {
      description = CHANNEL_DESC
    }.also {
      notificationManager.createNotificationChannel(it)
    }

    // Create the notification
    NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher_foreground)
      .setContentTitle("Clipbird Service")
      .setContentText("Tap to send the latest clipboard content to other devices")
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setContentIntent(onTap)
      .setOngoing(true)
      .addAction(0, "Send", onSend)
      .addAction(0, "Quit", onQuit)
      .build().also {
        notificationManager.notify(SERVICE_ID, it)
      }
  }

  /**
   * @brief Show the notification to the user
   */
  fun showJoinRequest(clientName: String, onAccept: PendingIntent, onReject: PendingIntent) {
    // Create the notification
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher_foreground)
      .setContentTitle("Connect Request")
      .setContentText("$clientName wants to connect to your device")
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setAutoCancel(true)
      .addAction(R.mipmap.ic_launcher_foreground, "Accept", onAccept)
      .addAction(R.mipmap.ic_launcher_foreground, "Reject", onReject)
      .build()

    // Show the notification
    notificationManager.notify(REQUEST_ID, notification)
  }
}
