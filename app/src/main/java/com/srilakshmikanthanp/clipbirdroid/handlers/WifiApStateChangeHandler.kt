package com.srilakshmikanthanp.clipbirdroid.handlers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import com.srilakshmikanthanp.clipbirdroid.common.enums.HostType
import com.srilakshmikanthanp.clipbirdroid.Clipbird

class WifiApStateChangeHandler : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == ACTION_WIFI_AP_STATE_CHANGED) {
      val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)
      if (WifiManager.WIFI_STATE_ENABLED == state % 10) {
        onHotspotEnabled(context.applicationContext as Clipbird)
      }
    }
  }

  private fun onHotspotEnabled(clipbird: Clipbird) {
    if (clipbird.getController().getHostType() == HostType.CLIENT) {
      clipbird.getController().restartBrowsing()
    }
  }

  companion object {
    const val ACTION_WIFI_AP_STATE_CHANGED = "android.net.wifi.WIFI_AP_STATE_CHANGED"
  }
}
