package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.service.ClipbirdService

class SendHandler : ComponentActivity() {
  private val connection = object : ServiceConnection {
    override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
      clipbirdBinder = binder as? ClipbirdService.ClipbirdBinder
      synchronized(this) {
        process()
      }
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
      clipbirdBinder = null
    }
  }

  private var clipbirdBinder: ClipbirdService.ClipbirdBinder? = null
  private var hasFocus = false
  private var hasProcessed = false

  private fun process() {
    val controller = clipbirdBinder?.getService()?.getController()
    if (!hasFocus || controller == null || hasProcessed) return
    controller.syncClipboard(controller.getClipboard())

    runOnUiThread {
      Toast.makeText(this, R.string.synced, Toast.LENGTH_SHORT).show()
      this.finish()
    }

    hasProcessed = true
  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    this.hasFocus = hasFocus
    synchronized(this) {
      process()
    }
  }

  override fun onStart() {
    super.onStart()

    Intent(this, ClipbirdService::class.java).also { intent ->
      bindService(intent, connection, BIND_AUTO_CREATE)
    }

    // show loading
    setContent {
      Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator()
        }
      }
    }
  }

  override fun onStop() {
    super.onStop()
    hasProcessed = false
    hasFocus = false
    if (clipbirdBinder != null) {
      unbindService(connection)
      clipbirdBinder = null
    }
  }
}
