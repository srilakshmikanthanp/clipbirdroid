package com.srilakshmikanthanp.clipbirdroid.syncing.manager

import com.srilakshmikanthanp.clipbirdroid.syncing.ClientServer
import com.srilakshmikanthanp.clipbirdroid.syncing.Session

sealed class ClientServerConnectionState {
  data object Idle : ClientServerConnectionState()
  data class Connecting(val server: ClientServer) : ClientServerConnectionState()
  data class Connected(val session: Session) : ClientServerConnectionState()
}
