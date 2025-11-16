package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import android.app.NotificationManager
import androidx.activity.ComponentActivity
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedClients
import com.srilakshmikanthanp.clipbirdroid.packets.AuthenticationPacket
import com.srilakshmikanthanp.clipbirdroid.packets.AuthenticationStatus
import com.srilakshmikanthanp.clipbirdroid.syncing.manager.SyncingManager
import com.srilakshmikanthanp.clipbirdroid.ui.gui.notification.ConnectionRequestNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AcceptHandler : ComponentActivity() {
  @Inject lateinit var syncingManager: SyncingManager
  @Inject lateinit var trustedClients: TrustedClients
  @Inject lateinit var coroutineScope: CoroutineScope

  override fun onStart() {
    super.onStart()
    val notify = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    val name = intent.getSerializableExtra(ACCEPT_EXTRA) as String
    val session = syncingManager.getServerClientSessionByName(name).orElseThrow()
    trustedClients.addTrustedClient(session.name, session.getCertificate())
    coroutineScope.launch { session.sendPacket(AuthenticationPacket(AuthenticationStatus.AuthOkay)) }
    notify.cancel(ConnectionRequestNotification.REQUEST_ID)
    this.finish()
  }

  companion object {
    const val ACCEPT_EXTRA = "com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.ACCEPT_EXTRA"
  }
}
