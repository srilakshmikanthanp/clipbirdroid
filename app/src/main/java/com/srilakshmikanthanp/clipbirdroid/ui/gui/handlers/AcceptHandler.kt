package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import com.srilakshmikanthanp.clipbirdroid.types.device.Device
import com.srilakshmikanthanp.clipbirdroid.ui.gui.service.ClipbirdService
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AcceptHandler : AbstractHandler() {
  override fun onConnectionReady(binder: ClipbirdService.ServiceBinder) {
    val controller = binder.getService().getController()
    val device = Json.decodeFromString<Device>(intent.getStringExtra(ACCEPT_EXTRA)!!)
    controller.onClientAuthenticated(device!!)
  }

  // Companion Object
  companion object {
    const val ACCEPT_EXTRA = "com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.ACCEPT_EXTRA"
  }
}
