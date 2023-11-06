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
class StatusNotification(private val context: Context) {
  // Notification Manager instance
  private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  // Notification channel constants
  companion object {
    const val CHANNEL_DESC = "This is the notification for Clipbird Service"
    const val CHANNEL_ID   = "StatusNotification"
    const val CHANNEL_NAME = "Clipbird"
    const val IMPORTANCE   = NotificationManager.IMPORTANCE_DEFAULT
    const val SERVICE_ID   = 1
    const val REQUEST_ID   = 2
  }

  /**
   * Initializer that creates Channel and Notification
   */
  init {
    NotificationChannel(CHANNEL_ID, CHANNEL_NAME, IMPORTANCE).apply {
      description = CHANNEL_DESC
    }.also {
      notificationManager.createNotificationChannel(it)
    }
  }

  /**
   * Get the Notification Channel ID
   */
  fun getChannelID(): String = CHANNEL_ID

  /**
   * @brief Show the notification to the user
   */
  fun showJoinRequest(clientName: String, onAccept: PendingIntent, onReject: PendingIntent) {
    // Create the notification
    NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher_foreground)
      .setContentTitle("Connect Request")
      .setContentText("$clientName wants to connect to your device")
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setAutoCancel(true)
      .addAction(R.mipmap.ic_launcher_foreground, "Accept", onAccept)
      .addAction(R.mipmap.ic_launcher_foreground, "Reject", onReject)
      .build().also {
        notificationManager.notify(REQUEST_ID, it)
      }
  }
}
