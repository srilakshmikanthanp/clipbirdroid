package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.srilakshmikanthanp.clipbirdroid.ui.gui.service.ClipbirdService

class BootHandler : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
      ClipbirdService.start(context)
    }
  }
}
