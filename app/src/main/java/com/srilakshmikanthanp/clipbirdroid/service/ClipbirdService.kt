package com.srilakshmikanthanp.clipbirdroid.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.srilakshmikanthanp.clipbirdroid.ApplicationState
import com.srilakshmikanthanp.clipbirdroid.broadcast.DeviceUnlockedHandler
import com.srilakshmikanthanp.clipbirdroid.clipboard.ClipboardManager
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedClients
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedServers
import com.srilakshmikanthanp.clipbirdroid.history.ClipboardHistory
import com.srilakshmikanthanp.clipbirdroid.packets.AuthenticationPacket
import com.srilakshmikanthanp.clipbirdroid.packets.AuthenticationStatus
import com.srilakshmikanthanp.clipbirdroid.syncing.ClientServer
import com.srilakshmikanthanp.clipbirdroid.syncing.manager.ClientServerConnectionState
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
import okio.IOException
import javax.inject.Inject

@AndroidEntryPoint
class ClipbirdService : Service() {
  private val serviceCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private val deviceUnlockedReceiver = DeviceUnlockedHandler()

  private lateinit var connectionRequestNotification: ConnectionRequestNotification
  private lateinit var notification: StatusNotification

  @Inject lateinit var clipboardManager: ClipboardManager
  @Inject lateinit var clipboardHistory: ClipboardHistory
  @Inject lateinit var syncingManager: SyncingManager
  @Inject lateinit var applicationState: ApplicationState
  @Inject lateinit var trustedClients: TrustedClients
  @Inject lateinit var trustedServers: TrustedServers

  inner class ClipbirdBinder : Binder() {
    fun getService(): ClipbirdService = this@ClipbirdService
  }

  val binder = ClipbirdBinder()

  private suspend fun connect(server: ClientServer) {
    try {
      syncingManager.connectToServer(server)
    } catch (e: IOException) {
      Toast.makeText(this@ClipbirdService, "Failed to connect to trusted server ${server.name}", Toast.LENGTH_SHORT).show()
    }
  }

  private fun initialize() {
    this.connectionRequestNotification = ConnectionRequestNotification(this)
    this.notification = StatusNotification(this)

    this.serviceCoroutineScope.launch {
      syncingManager.serverFoundEvents.collect {
        if (
          syncingManager.serverState.value == ClientServerConnectionState.Idle &&
          trustedServers.hasTrustedServer(it.name)
        ) {
          connect(it)
        }
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

    syncingManager.addSyncRequestHandler {
      clipboardHistory.addHistory(it)
    }

    this.serviceCoroutineScope.launch {
      clipboardHistory.clipboard.collect {
        clipboardManager.getClipboard().setClipboardContent(it)
      }
    }

    this.serviceCoroutineScope.launch {
      clipboardManager.clipboardChangeEvents.collect {
        syncingManager.synchronize(it)
      }
    }

    this.serviceCoroutineScope.launch {
      combine(
        applicationState.shouldUseBluetoothFlow,
        applicationState.isServerFlow
      ) {
        useBluetooth, isServer -> isServer to useBluetooth
      }.collect { (isServer, useBluetooth) ->
        if (isServer) {
          syncingManager.setHostAsServer(useBluetooth)
        } else {
          syncingManager.setHostAsClient(useBluetooth)
        }
      }
    }

    registerReceiver(
      deviceUnlockedReceiver,
      IntentFilter(Intent.ACTION_USER_PRESENT)
    )

    this.showStatusNotification()
  }

  override fun onCreate() {
    super.onCreate()
    this.initialize()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent?.action == ACTION_DEVICE_UNLOCKED) {
      this.serviceCoroutineScope.launch {
        syncingManager.availableServers.collect { servers ->
          servers.forEach {
            if (
              syncingManager.serverState.value == ClientServerConnectionState.Idle &&
              trustedServers.hasTrustedServer(it.name) &&
              applicationState.getPrimaryServer() == it.name
            ) {
              connect(it)
            }
          }
        }
      }
    }
    return START_STICKY
  }

  override fun onDestroy() {
    super.onDestroy()
    unregisterReceiver(deviceUnlockedReceiver)
    this.serviceCoroutineScope.cancel()
  }

  override fun onBind(intent: Intent?): IBinder = binder

  fun showStatusNotification() {
    notification.showStatusNotification(this)
  }

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

    const val ACTION_DEVICE_UNLOCKED = "com.srilakshmikanthanp.clipbirdroid.DEVICE_UNLOCKED"
  }
}
