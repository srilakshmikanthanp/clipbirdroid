package com.srilakshmikanthanp.clipbirdroid.handlers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.srilakshmikanthanp.clipbirdroid.ui.gui.Clipbird
import com.srilakshmikanthanp.clipbirdroid.service.ClipbirdService

class BootHandler : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    Log.d("BootHandler", "Initializing Application: ${intent.action}")
    (context.applicationContext as Clipbird).initialize()
    Log.d("BootHandler", "Starting Service: ${intent.action}")
    ClipbirdService.start(context)
    Log.d("BootHandler", "Started Service: ${intent.action}")
  }
}
