package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import android.app.NotificationManager
import android.content.Context
import androidx.activity.ComponentActivity
import com.srilakshmikanthanp.clipbirdroid.types.Device
import com.srilakshmikanthanp.clipbirdroid.ui.gui.Clipbird
import com.srilakshmikanthanp.clipbirdroid.ui.gui.notify.StatusNotification

class RejectHandler : ComponentActivity() {
  override fun onStart() {
    super.onStart()
    val notify = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val controller = (this.application as Clipbird).getController()
    val device = intent.getSerializableExtra(REJECT_EXTRA) as Device?
    controller.onClientNotAuthenticated(device!!)
    notify.cancel(StatusNotification.REQUEST_ID)
    this.finish()
  }

  // Companion Object
  companion object {
    const val REJECT_EXTRA = "com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.REJECT_EXTRA"
  }
}
