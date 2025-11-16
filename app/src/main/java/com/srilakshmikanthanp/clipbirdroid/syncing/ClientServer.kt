package com.srilakshmikanthanp.clipbirdroid.syncing

abstract class ClientServer(val name: String) {
  abstract suspend fun connect(listener: ClientServerSessionEventListener)
}
