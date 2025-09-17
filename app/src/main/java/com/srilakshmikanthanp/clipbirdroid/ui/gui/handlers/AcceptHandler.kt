package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.activity.ComponentActivity
import com.srilakshmikanthanp.clipbirdroid.common.types.Device
import com.srilakshmikanthanp.clipbirdroid.service.ClipbirdService
import com.srilakshmikanthanp.clipbirdroid.ui.gui.notification.StatusNotification

class AcceptHandler : ComponentActivity() {
  private val connection = object : ServiceConnection {
    override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
      isBound = true
      val clipbirdBinder = binder as? ClipbirdService.ClipbirdBinder ?: return
      val service = clipbirdBinder.getService()
      val controller = service.getController()
      val notify = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
      val device = intent.getSerializableExtra(ACCEPT_EXTRA) as Device?
      controller.onClientAuthenticated(device!!)
      notify.cancel(StatusNotification.REQUEST_ID)
      finish()
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
      isBound = false
    }
  }

  private var isBound = false

  override fun onStart() {
    super.onStart()
    Intent(this, ClipbirdService::class.java).also { intent ->
      bindService(intent, connection, BIND_AUTO_CREATE)
    }
  }

  override fun onStop() {
    super.onStop()
    if (isBound) {
      unbindService(connection)
      isBound = false
    }
  }

  // Companion Object
  companion object {
    const val ACCEPT_EXTRA = "com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.ACCEPT_EXTRA"
  }
}
