package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import android.app.NotificationManager
import android.content.Context
import androidx.activity.ComponentActivity
import com.srilakshmikanthanp.clipbirdroid.common.types.Device
import com.srilakshmikanthanp.clipbirdroid.Clipbird
import com.srilakshmikanthanp.clipbirdroid.ui.gui.notification.StatusNotification

class AcceptHandler : ComponentActivity() {
  override fun onStart() {
    super.onStart()
    val notify = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val controller = (this.application as Clipbird).getController()
    val device = intent.getSerializableExtra(ACCEPT_EXTRA) as Device?
    controller.onClientAuthenticated(device!!)
    notify.cancel(StatusNotification.REQUEST_ID)
    this.finish()
  }

  // Companion Object
  companion object {
    const val ACCEPT_EXTRA = "com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.ACCEPT_EXTRA"
  }
}
