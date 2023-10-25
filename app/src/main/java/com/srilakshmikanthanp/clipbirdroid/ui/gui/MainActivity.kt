package com.srilakshmikanthanp.clipbirdroid.ui.gui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.srilakshmikanthanp.clipbirdroid.R


class MyService : Service() {
  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  private fun showNotification(context: Context) {
    val channelId = "clipbird"
    // create channel
    val notificationChannel = NotificationChannel(
      channelId,
      "Clipboard",
      NotificationManager.IMPORTANCE_DEFAULT
    )

    val notificationBuilder = NotificationCompat.Builder(context, channelId)
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setContentTitle("Notification Title")
      .setContentText("Click the button to Copy!")
      .addAction(R.drawable.ic_launcher_foreground, "Copy", getHelloPendingIntent(context))
      .setOngoing(true)

    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.createNotificationChannel(notificationChannel)
    notificationManager.notify(1, notificationBuilder.build())


    Log.d("MyService", "showNotification")
  }

  private fun getHelloPendingIntent(context: Context): PendingIntent {
    val helloIntent = Intent(context, SendHandler::class.java)
    return PendingIntent.getActivity(context, 100, helloIntent, PendingIntent.FLAG_UPDATE_CURRENT)
  }

  override fun onCreate() {
    super.onCreate()
    showNotification(this)
  }
}

class SendHandler : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  // on focus
  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)

    if(!hasFocus) return;

    Log.d("SendHandler", "onWindowFocusChanged")

    // print the clipboard content
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = clipboard.primaryClip
    if (clip != null) {
      val item = clip.getItemAt(0)
      val text = item.text
      Log.d("SendHandler", "text: $text")
    }

    // finished
    finish()
  }
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        // A surface container using the 'background' color from the theme
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          Greeting("Android")
        }
      }
    }
    // start the service
    val intent = Intent(this, MyService::class.java)
    startService(intent)
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(
    text = "Hello $name!",
    modifier = modifier
  )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MaterialTheme {
    Greeting("Android")
  }
}
