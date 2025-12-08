package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import android.app.NotificationManager
import android.content.Context
import androidx.activity.ComponentActivity
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedClients
import com.srilakshmikanthanp.clipbirdroid.packets.AuthenticationPacket
import com.srilakshmikanthanp.clipbirdroid.packets.AuthenticationStatus
import com.srilakshmikanthanp.clipbirdroid.syncing.manager.SyncingManager
import com.srilakshmikanthanp.clipbirdroid.ui.gui.notification.ConnectionRequestNotification
import com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.AcceptHandler.Companion.ACCEPT_EXTRA
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RejectHandler : ComponentActivity() {
  @Inject lateinit var syncingManager: SyncingManager
  @Inject lateinit var trustedClients: TrustedClients
  @Inject lateinit var coroutineScope: CoroutineScope

  override fun onStart() {
    super.onStart()
    val notify = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val name = intent.getSerializableExtra(REJECT_EXTRA) as String
    val session = syncingManager.getServerClientSessionByName(name).orElseThrow()
    coroutineScope.launch { session.sendPacket(AuthenticationPacket(AuthenticationStatus.AuthFail)) }
    notify.cancel(ConnectionRequestNotification.REQUEST_ID)
    this.finish()
  }

  companion object {
    const val REJECT_EXTRA = "com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.REJECT_EXTRA"
  }
}
