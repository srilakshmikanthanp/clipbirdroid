package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.srilakshmikanthanp.clipbirdroid.ui.gui.utilities.ClipbirdServiceConnection
import com.srilakshmikanthanp.clipbirdroid.ui.gui.service.ClipbirdService
import kotlinx.coroutines.launch

class SendHandler : ComponentActivity() {
  // Service connection to the StatusNotification
  private val serviceConnection: ClipbirdServiceConnection = ClipbirdServiceConnection()

  // Bind the service
  private fun setUpService() {
    Intent(this, ClipbirdService::class.java).also { intent ->
      bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }
  }

  // process the intent
  private fun processIntent(isBound: Boolean) {
    if (isBound) {
      val controller = serviceConnection.getBinder()!!.getService().getController()
      controller.syncClipboard(controller.getClipboard())
      finish()
    }
  }

  // On Create
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState).also { setUpService() }
    setContent { }
  }

  // On Window Focus Changed
  override fun onWindowFocusChanged(hasFocus: Boolean) {
    if (!hasFocus) return // If the window has no focus, return

    // Otherwise, process the intent
    lifecycleScope.launch {
      serviceConnection.isBound().collect { isBound ->
        processIntent(isBound)
      }
    }
  }
}
