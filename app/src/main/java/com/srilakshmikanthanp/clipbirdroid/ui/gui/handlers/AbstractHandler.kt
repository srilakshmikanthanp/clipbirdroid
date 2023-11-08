package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.srilakshmikanthanp.clipbirdroid.ui.gui.service.ClipbirdService
import com.srilakshmikanthanp.clipbirdroid.ui.gui.utilities.ClipbirdServiceConnection
import kotlinx.coroutines.launch

/**
 * An abstract handler for the ClipbirdService
 */
abstract class AbstractHandler : ComponentActivity() {
  // Service connection to the StatusNotification
  private val connection: ClipbirdServiceConnection = ClipbirdServiceConnection()

  // Notification Manager instance
  private lateinit var notificationManager : NotificationManager

  // Bind the service
  private fun setUpService() {
    this.getSystemService(Context.NOTIFICATION_SERVICE).also { manager ->
      notificationManager = manager as NotificationManager
    }

    Intent(this, ClipbirdService::class.java).also { intent ->
      bindService(intent, connection, BIND_AUTO_CREATE)
    }
  }

  // Unbind the service
  private fun disposeService() {
    unbindService(connection)
  }

  // OnController Ready
  open fun onConnectionReady(binder: ClipbirdService.ServiceBinder) {
    Log.i("AbstractHandler", "Controller Ready")
  }

  // On Create
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState).also { setUpService() }
  }

  // on Destroy
  override fun onDestroy() {
    super.onDestroy().also { disposeService() }
  }

  // get the notification manager
  fun getNotificationManager(): NotificationManager = notificationManager

  // On Window Focus Changed
  override fun onWindowFocusChanged(hasFocus: Boolean) {
    if (hasFocus) lifecycleScope.launch {
      connection.isBound().collect { if(it) onConnectionReady(connection.getBinder()!!) }
    }
  }
}
