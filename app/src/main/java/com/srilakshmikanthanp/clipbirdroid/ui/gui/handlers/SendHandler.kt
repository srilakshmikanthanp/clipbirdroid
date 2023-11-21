package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import android.widget.Toast
import androidx.activity.ComponentActivity
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.ui.gui.Clipbird

class SendHandler : ComponentActivity() {
  override fun onWindowFocusChanged(hasFocus: Boolean) {
    if (!hasFocus) return
    val controller = (this.application as Clipbird).getController()
    controller.syncClipboard(controller.getClipboard())
    Toast.makeText(this, R.string.synced, Toast.LENGTH_SHORT).show()
    this.finish()
  }
}
