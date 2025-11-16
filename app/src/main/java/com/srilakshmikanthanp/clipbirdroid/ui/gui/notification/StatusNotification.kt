package com.srilakshmikanthanp.clipbirdroid.ui.gui.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.ui.gui.MainActivity
import com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.SendHandler

class StatusNotification(private val context: Context) {
  private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  private val channelDescription = context.resources.getString(R.string.notification_content)
  private val channelId = context.resources.getString(R.string.status_notification)
  private val channelName = context.resources.getString(R.string.app_name)
  private val importance = NotificationManager.IMPORTANCE_LOW

  companion object {
    const val SERVICE_ID = 1
  }

  private fun onQuitIntent(): PendingIntent {
    val intent = Intent(context, MainActivity::class.java)
    val flags = PendingIntent.FLAG_IMMUTABLE
    intent.action = MainActivity.QUIT_ACTION
    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
    return PendingIntent.getActivity(context, 0, intent, flags)
  }

  private fun onSendIntent(): PendingIntent {
    Intent(context, SendHandler::class.java).also {
      return PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
    }
  }

  private fun onTapIntent(): PendingIntent {
    Intent(context, MainActivity::class.java).also {
      return PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
    }
  }

  init {
    NotificationChannel(channelId, channelName, importance).apply { description = channelDescription }.also {
      notificationManager.createNotificationChannel(it)
    }
  }

  fun showStatusNotification(service: Service) {
    val notificationLayout = RemoteViews(context.packageName, R.layout.notification)

    notificationLayout.setTextViewText(R.id.notify_title, context.resources.getString(R.string.notification_title))
    notificationLayout.setOnClickPendingIntent(R.id.notify_send, onSendIntent())

    NotificationCompat.Builder(context, channelId)
      .setSmallIcon(R.mipmap.ic_launcher_foreground)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setStyle(NotificationCompat.DecoratedCustomViewStyle())
      .setCustomContentView(notificationLayout)
      .setContentIntent(onTapIntent())
      .addAction(
        R.mipmap.ic_launcher_foreground,
        context.resources.getString(R.string.quit),
        onQuitIntent()
      )
      .setOngoing(true)
      .build().also {
        service.startForeground(SERVICE_ID, it)
      }
  }
}
