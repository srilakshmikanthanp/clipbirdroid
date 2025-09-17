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

class RejectHandler : ComponentActivity() {
  private val connection = object : ServiceConnection {
    override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
      isBound = true
      val clipbirdBinder = binder as? ClipbirdService.ClipbirdBinder ?: return
      val notify = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
      val controller = clipbirdBinder.getService().getController()
      val device = intent.getSerializableExtra(REJECT_EXTRA) as Device?
      controller.onClientNotAuthenticated(device!!)
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

  companion object {
    const val REJECT_EXTRA = "com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.REJECT_EXTRA"
  }
}
