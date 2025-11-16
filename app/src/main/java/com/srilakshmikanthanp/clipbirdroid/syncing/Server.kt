package com.srilakshmikanthanp.clipbirdroid.syncing

import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig

abstract class Server(protected val context: Context, protected val sslConfig: SSLConfig) {
  protected val serverEventListeners = mutableListOf<ServerEventListener>()

  fun addServerEventListener(listener: ServerEventListener) {
    serverEventListeners.add(listener)
  }

  fun removeServerEventListener(listener: ServerEventListener) {
    serverEventListeners.remove(listener)
  }

  abstract suspend fun start()
  abstract suspend fun stop()
}
