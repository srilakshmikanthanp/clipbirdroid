package com.srilakshmikanthanp.clipbirdroid.ui.gui.notification

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
  private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  private val channelDescription = context.resources.getString(R.string.notification_content)
  private val channelId = context.resources.getString(R.string.status_notification)
  private val channelName = context.resources.getString(R.string.app_name)
  private val importance = NotificationManager.IMPORTANCE_LOW

  companion object {
    const val SERVICE_ID = 1
    const val REQUEST_ID = 2
  }

  init {
    NotificationChannel(channelId, channelName, importance).apply {
      description = channelDescription
    }.also {
      notificationManager.createNotificationChannel(it)
    }
  }

  fun getChannelID(): String = channelId

  fun showJoinRequest(clientName: String, onAccept: PendingIntent, onReject: PendingIntent) {
    NotificationCompat.Builder(context, channelId)
      .setSmallIcon(R.mipmap.ic_launcher_foreground)
      .setContentTitle(context.resources.getString(R.string.connection_request))
      .setContentText("$clientName " + context.resources.getString(R.string.join_request_content))
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setAutoCancel(true)
      .addAction(
        R.mipmap.ic_launcher_foreground,
        context.resources.getString(R.string.accept),
        onAccept
      )
      .addAction(
        R.mipmap.ic_launcher_foreground,
        context.resources.getString(R.string.reject),
        onReject
      )
      .build().also {
        notificationManager.notify(REQUEST_ID, it)
      }
  }
}
