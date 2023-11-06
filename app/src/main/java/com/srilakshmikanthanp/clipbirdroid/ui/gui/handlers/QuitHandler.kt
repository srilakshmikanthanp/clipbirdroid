package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import com.srilakshmikanthanp.clipbirdroid.ui.gui.service.ClipbirdService

class QuitHandler : AbstractHandler() {
  override fun onConnectionReady(binder: ClipbirdService.ServiceBinder) {
    binder.getService().stopSelf()
  }
}
