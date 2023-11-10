package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import com.srilakshmikanthanp.clipbirdroid.types.device.Device
import com.srilakshmikanthanp.clipbirdroid.ui.gui.notifications.StatusNotification
import com.srilakshmikanthanp.clipbirdroid.ui.gui.service.ClipbirdService

class AcceptHandler : AbstractHandler() {
  override fun onConnectionReady(binder: ClipbirdService.ServiceBinder) {
    val controller = binder.getService().getController()
    val device = intent.getSerializableExtra(ACCEPT_EXTRA) as Device?
    controller.onClientAuthenticated(device!!)
    getNotificationManager().cancel(StatusNotification.REQUEST_ID)
    this.finish()
  }

  // Companion Object
  companion object {
    const val ACCEPT_EXTRA = "com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.ACCEPT_EXTRA"
  }
}