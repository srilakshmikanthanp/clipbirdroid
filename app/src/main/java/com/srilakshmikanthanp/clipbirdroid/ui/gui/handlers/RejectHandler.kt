package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import com.srilakshmikanthanp.clipbirdroid.types.device.Device
import com.srilakshmikanthanp.clipbirdroid.ui.gui.service.ClipbirdService
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class RejectHandler : AbstractHandler() {
  override fun onConnectionReady(binder: ClipbirdService.ServiceBinder) {
    val controller = binder.getService().getController()
    val device = Json.decodeFromString<Device>(intent.getStringExtra(REJECT_EXTRA)!!)
    controller.onClientNotAuthenticated(device!!)
  }

  // Companion Object
  companion object {
    const val REJECT_EXTRA = "REJECT_EXTRA"
  }
}
