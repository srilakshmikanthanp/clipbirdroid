package com.srilakshmikanthanp.clipbirdroid.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.srilakshmikanthanp.clipbirdroid.service.ClipbirdService

class DeviceUnlockedHandler : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    Intent(context, ClipbirdService::class.java).apply {
      action = ClipbirdService.ACTION_DEVICE_UNLOCKED
    }.also {
      context.startForegroundService(it)
    }
  }
}
