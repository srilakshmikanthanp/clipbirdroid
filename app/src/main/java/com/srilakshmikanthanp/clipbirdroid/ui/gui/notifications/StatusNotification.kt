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
  private val CHANNEL_DESC = context.resources.getString(R.string.notification_content)
  private val CHANNEL_ID   = context.resources.getString(R.string.status_notification)
  private val CHANNEL_NAME = context.resources.getString(R.string.app_name)
  private val IMPORTANCE   = NotificationManager.IMPORTANCE_DEFAULT

  // Companion object
  companion object {
    val SERVICE_ID   = 1
    val REQUEST_ID   = 2
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
      .setContentTitle(context.resources.getString(R.string.connection_request))
      .setContentText("$clientName "+context.resources.getString(R.string.join_request_content))
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setAutoCancel(true)
      .addAction(R.mipmap.ic_launcher_foreground, context.resources.getString(R.string.accept), onAccept)
      .addAction(R.mipmap.ic_launcher_foreground, context.resources.getString(R.string.reject), onReject)
      .build().also {
        notificationManager.notify(REQUEST_ID, it)
      }
  }
}
