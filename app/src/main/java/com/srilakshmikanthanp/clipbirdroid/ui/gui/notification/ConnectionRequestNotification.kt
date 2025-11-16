package com.srilakshmikanthanp.clipbirdroid.ui.gui.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.syncing.Session
import com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.AcceptHandler
import com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.RejectHandler

class ConnectionRequestNotification(private val context: Context) {
  private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  private val channelDescription = context.resources.getString(R.string.connection_request_channel_description)
  private val channelName = context.resources.getString(R.string.connection_request)
  private val channelId = ConnectionRequestNotification::class.java.name
  private val importance = NotificationManager.IMPORTANCE_MIN

  private fun onAcceptIntent(session: Session): PendingIntent {
    val intent = Intent(context, AcceptHandler::class.java)
    val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    intent.putExtra(AcceptHandler.ACCEPT_EXTRA, session.name)
    return PendingIntent.getActivity(context, 0, intent, flags)
  }

  private fun onRejectIntent(session: Session): PendingIntent {
    val intent = Intent(context, RejectHandler::class.java)
    val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    intent.putExtra(RejectHandler.REJECT_EXTRA, session.name)
    return PendingIntent.getActivity(context, 0, intent, flags)
  }

  companion object {
    const val REQUEST_ID = 2
  }

  init {
    NotificationChannel(channelId, channelName, importance).apply { description = channelDescription }.also {
      notificationManager.createNotificationChannel(it)
    }
  }

  fun showJoinRequest(session: Session) {
    NotificationCompat.Builder(context, channelId)
      .setSmallIcon(R.mipmap.ic_launcher_foreground)
      .setContentTitle(context.resources.getString(R.string.connection_request))
      .setContentText("${session.name} " + context.resources.getString(R.string.join_request_content))
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setAutoCancel(true)
      .addAction(
        R.mipmap.ic_launcher_foreground,
        context.resources.getString(R.string.accept),
        onAcceptIntent(session)
      )
      .addAction(
        R.mipmap.ic_launcher_foreground,
        context.resources.getString(R.string.reject),
        onRejectIntent(session)
      )
      .build().also {
        notificationManager.notify(REQUEST_ID, it)
      }
  }
}
