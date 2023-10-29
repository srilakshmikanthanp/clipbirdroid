package com.srilakshmikanthanp.clipbirdroid.ui.gui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.controller.AppController
import com.srilakshmikanthanp.clipbirdroid.types.device.Device
import com.srilakshmikanthanp.clipbirdroid.utility.functions.generateX509Certificate
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.net.InetAddress
import java.security.Security


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
    if (ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.POST_NOTIFICATIONS
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      // TODO: Consider calling
      //    ActivityCompat#requestPermissions
      // here to request the missing permissions, and then overriding
      //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
      //                                          int[] grantResults)
      // to handle the case where the user grants the permission. See the documentation
      // for ActivityCompat#requestPermissions for more details.
      return
    }
    notificationManager.notify(1, notificationBuilder.build())


    Log.d("MyService", "showNotification")
  }

  private fun getHelloPendingIntent(context: Context): PendingIntent {
    val helloIntent = Intent(context, SendHandler::class.java)
    return PendingIntent.getActivity(context, 100, helloIntent, PendingIntent.FLAG_UPDATE_CURRENT)
  }

  override fun onCreate() {
    super.onCreate()

    val ssl = generateX509Certificate(1024)

    val controller = AppController(ssl, this)

    controller.setCurrentHostAsClient()

    // 192.168.206.45
    controller.connectToServer(
      Device(
        InetAddress.getByName("192.168.206.45"), 64535, "LAPTOP-JC2M372A"
      )
    )

    controller.addServerStatusChangedHandler{
      if (it) {
        val i = controller.getConnectedServer()
        Log.d("MyService", "Server is running: ${i.ip}, ${i.port}")
      } else {
        Log.d("MyService", "Server is not running")
      }
    }

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
    Security.addProvider(
      BouncyCastleProvider()
    )
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
