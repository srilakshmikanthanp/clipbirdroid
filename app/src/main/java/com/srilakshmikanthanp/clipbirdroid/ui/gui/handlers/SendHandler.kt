package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import com.srilakshmikanthanp.clipbirdroid.ui.gui.service.ClipbirdService

class SendHandler : AbstractHandler() {
  override fun onConnectionReady(binder: ClipbirdService.ServiceBinder) {
    binder.getService().getController().also {
      it.syncClipboard(it.getClipboard()).also { this.finish() }
    }
  }
}
