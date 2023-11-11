package com.srilakshmikanthanp.clipbirdroid.ui.gui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.compose.ClipbirdTheme
import com.srilakshmikanthanp.clipbirdroid.ui.gui.screens.SettingUp
import com.srilakshmikanthanp.clipbirdroid.ui.gui.service.ClipbirdService
import com.srilakshmikanthanp.clipbirdroid.ui.gui.utilities.ClipbirdServiceConnection

class MainActivity : ComponentActivity() {
  // Service connection to the StatusNotification
  private val serviceConnection: ClipbirdServiceConnection = ClipbirdServiceConnection()

  // companion object
  companion object {
    val QUIT_ACTION = "com.srilakshmikanthanp.clipbirdroid.ui.gui.MainActivity.QUIT_ACTION"
  }

  // Set up the Service Connection
  private fun setUpService() {
    val intent = Intent(this, ClipbirdService::class.java)
    startForegroundService(intent)
    bindService(intent, serviceConnection, BIND_AUTO_CREATE)
  }

  // dispose the service
  private fun stopService() {
    stopService(Intent(this, ClipbirdService::class.java))
  }

  // set up the UI
  @Composable
  private fun SetUpUI() {
    val isServiceConnected by serviceConnection.isBound().collectAsState()
    if (!isServiceConnected) SettingUp().also { return }
    Clipbird(serviceConnection.getBinder()!!.getService().getController())
  }

  // On Create
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setUpService()
    setContent { ClipbirdTheme { SetUpUI() } }
  }

  // on Start
  override fun onStart() {
    super.onStart().also {
      if (intent?.action == QUIT_ACTION) stopService().also { this.finishAffinity() }
    }
  }

  // On Destroy unbind the service
  override fun onDestroy() {
    super.onDestroy().also { unbindService(serviceConnection) }
  }
}
