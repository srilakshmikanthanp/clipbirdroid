package com.srilakshmikanthanp.clipbirdroid.ui.gui

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.srilakshmikanthanp.clipbirdroid.ui.gui.helpers.ClipbirdServiceConnection
import com.srilakshmikanthanp.clipbirdroid.ui.gui.screens.SettingUp
import com.srilakshmikanthanp.clipbirdroid.ui.gui.service.ClipbirdService

class MainActivity : ComponentActivity() {
  // Service connection to the StatusNotification
  private val serviceConnection: ClipbirdServiceConnection = ClipbirdServiceConnection()

  // Set Up the UI and the Service
  private fun setUpActivity() {
    // Create Service
    Intent(this, ClipbirdService::class.java).also { intent ->
      startForegroundService(intent)
    }

    // Bind the service
    Intent(this, ClipbirdService::class.java).also { intent ->
      bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }

    // Set the content
    setContent {
      val isServiceConnected by serviceConnection.isBound().collectAsState()
      if (!isServiceConnected) { SettingUp().also { return@setContent } }
      Clipbird(serviceConnection.getBinder()!!.getService().getController())
    }
  }

  // On start
  override fun onStart() {
    super.onStart().also { setUpActivity() }
  }
}
