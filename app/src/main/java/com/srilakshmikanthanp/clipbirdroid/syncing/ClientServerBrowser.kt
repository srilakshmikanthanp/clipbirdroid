package com.srilakshmikanthanp.clipbirdroid.syncing

import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig

abstract class ClientServerBrowser(protected val context: Context, protected val sslConfig: SSLConfig) {
  protected val clientEventListeners = mutableListOf<ClientServerBrowserEventListener>()

  fun addClientServerBrowserEventListener(listener: ClientServerBrowserEventListener) {
    clientEventListeners.add(listener)
  }

  fun removeClientServerBrowserEventListener(listener: ClientServerBrowserEventListener) {
    clientEventListeners.remove(listener)
  }

  abstract suspend fun start()
  abstract suspend fun stop()
}
