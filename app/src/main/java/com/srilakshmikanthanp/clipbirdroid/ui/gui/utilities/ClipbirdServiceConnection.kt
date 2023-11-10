package com.srilakshmikanthanp.clipbirdroid.ui.gui.utilities

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.srilakshmikanthanp.clipbirdroid.ui.gui.service.ClipbirdService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ClipbirdServiceConnection : ServiceConnection {
  // Binder instance of the StatusNotification
  private var binder: ClipbirdService.ServiceBinder? = null

  // LiveData for the service connection status
  private var isBound = MutableStateFlow(false)

  // Called when the service is connected
  override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
    isBound.value = true.also { binder = service as ClipbirdService.ServiceBinder }
  }

  // Called when the service is disconnected
  override fun onServiceDisconnected(p0: ComponentName?) {
    isBound.value = false.also { binder = null; }
  }

  // is the service connected
  fun isBound(): StateFlow<Boolean> = isBound

  // get the binder
  fun getBinder(): ClipbirdService.ServiceBinder? = binder
}
