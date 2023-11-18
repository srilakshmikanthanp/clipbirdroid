package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import androidx.activity.ComponentActivity
import com.srilakshmikanthanp.clipbirdroid.ui.gui.Clipbird

class SendHandler : ComponentActivity() {
  override fun onWindowFocusChanged(hasFocus: Boolean) {
    if (!hasFocus) return
    val controller = (this.application as Clipbird).getController()
    controller.syncClipboard(controller.getClipboard())
    this.finish()
  }
}
