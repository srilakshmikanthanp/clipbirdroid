package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import android.app.NotificationManager
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedClient
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

  private suspend fun handle(name: String) {
    val session = syncingManager.getServerClientSessionByName(name).orElseThrow()
    trustedClients.addTrustedClient(TrustedClient(session.name, session.getCertificate()))
    session.sendPacket(AuthenticationPacket(AuthenticationStatus.AuthOkay))
    val notify = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    notify.cancel(ConnectionRequestNotification.REQUEST_ID)
  }

  override fun onStart() {
    super.onStart()
    val name = intent.getSerializableExtra(ACCEPT_EXTRA) as String
    coroutineScope.launch { handle(name) }
    this.finish()
  }

  companion object {
    const val ACCEPT_EXTRA = "com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.ACCEPT_EXTRA"
  }
}
