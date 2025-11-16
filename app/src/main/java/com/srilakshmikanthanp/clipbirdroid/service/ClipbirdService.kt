package com.srilakshmikanthanp.clipbirdroid.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.srilakshmikanthanp.clipbirdroid.ApplicationState
import com.srilakshmikanthanp.clipbirdroid.clipboard.ClipboardManager
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedClients
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedServers
import com.srilakshmikanthanp.clipbirdroid.history.ClipboardHistory
import com.srilakshmikanthanp.clipbirdroid.packets.AuthenticationPacket
import com.srilakshmikanthanp.clipbirdroid.packets.AuthenticationStatus
import com.srilakshmikanthanp.clipbirdroid.syncing.ClientServer
import com.srilakshmikanthanp.clipbirdroid.syncing.manager.SyncingManager
import com.srilakshmikanthanp.clipbirdroid.ui.gui.notification.ConnectionRequestNotification
import com.srilakshmikanthanp.clipbirdroid.ui.gui.notification.StatusNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ClipbirdService : Service() {
  private val serviceCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

  private lateinit var connectionRequestNotification: ConnectionRequestNotification
  private lateinit var notification: StatusNotification

  @Inject lateinit var clipboardManager: ClipboardManager
  @Inject lateinit var clipboardHistory: ClipboardHistory
  @Inject lateinit var syncingManager: SyncingManager
  @Inject lateinit var applicationState: ApplicationState
  @Inject lateinit var trustedClients: TrustedClients
  @Inject lateinit var trustedServers: TrustedServers

  private val servers: MutableList<ClientServer> = mutableListOf()

  inner class ClipbirdBinder : Binder() {
    fun getService(): ClipbirdService = this@ClipbirdService
  }

  val binder = ClipbirdBinder()

  private fun initialize() {
    this.serviceCoroutineScope.launch {
      syncingManager.disconnectedEvents.collect { server ->
        servers.firstOrNull { trustedServers.hasTrustedServer(it.name) && it.name != server.name }?.let {
          syncingManager.connectToServer(it)
        }
      }
    }

    syncingManager.addSyncRequestHandler {
      clipboardHistory.addHistory(it)
    }

    this.serviceCoroutineScope.launch {
      clipboardHistory.clipboard.collect {
        clipboardManager.getClipboard().setClipboardContent(it)
      }
    }

    this.serviceCoroutineScope.launch {
      syncingManager.serverFoundEvents.collect {
        if (trustedServers.hasTrustedServer(it.name) && !syncingManager.isConnectedToServer()) syncingManager.connectToServer(it)
        servers.add(it)
      }
    }

    this.serviceCoroutineScope.launch {
      syncingManager.serverGoneEvents.collect {
        servers.remove(it)
      }
    }

    this.serviceCoroutineScope.launch {
      syncingManager.clientConnectedEvents.collect {
        if (it.isTrusted.value) {
          it.sendPacket(AuthenticationPacket(AuthenticationStatus.AuthOkay))
        } else {
          connectionRequestNotification.showJoinRequest(it)
        }
      }
    }

    this.serviceCoroutineScope.launch {
      clipboardManager.clipboardChangeEvents.collect {
        syncingManager.synchronize(it)
      }
    }

    this.serviceCoroutineScope.launch {
      combine(applicationState.shouldUseBluetoothFlow, applicationState.isServerFlow) { useBluetooth, isServer ->
        isServer to useBluetooth
      }.collect { (isServer, useBluetooth) ->
        if (isServer) {
          syncingManager.setHostAsServer(useBluetooth)
        } else {
          syncingManager.setHostAsClient(useBluetooth)
        }
      }
    }
  }

  override fun onCreate() {
    super.onCreate()
    this.connectionRequestNotification = ConnectionRequestNotification(this)
    this.notification = StatusNotification(this)
    this.initialize()
    notification.showStatusNotification(this)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    return START_STICKY
  }

  override fun onDestroy() {
    super.onDestroy()
    this.serviceCoroutineScope.cancel()
  }

  override fun onBind(intent: Intent?): IBinder = binder

  companion object {
    fun start(context: Context) {
      Intent(context, ClipbirdService::class.java).also {
        context.startForegroundService(it)
      }
    }

    fun stop(context: Context) {
      Intent(context, ClipbirdService::class.java).also {
        context.stopService(it)
      }
    }
  }
}
