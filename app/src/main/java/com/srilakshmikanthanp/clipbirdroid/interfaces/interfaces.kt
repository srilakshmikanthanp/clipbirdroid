package com.srilakshmikanthanp.clipbirdroid.interfaces

import com.srilakshmikanthanp.clipbirdroid.types.Device
import com.srilakshmikanthanp.clipbirdroid.types.enums.HostType

// Interface for On Server List Changed
fun interface OnServerListChangeHandler {
  fun onServerListChanged(servers: List<Device>)
}

// Interface for On Server Found
fun interface OnServerFoundHandler {
  fun onServerFound(server: Device)
}

// Interface for On Server Gone
fun interface OnServerGoneHandler {
  fun onServerGone(server: Device)
}

// Interface for On Server state changed
fun interface OnServerStatusChangeHandler {
  fun onServerStatusChanged(isConnected: Boolean, server: Device)
}

// Interface for On Connection Error
fun interface OnConnectionErrorHandler {
  fun onConnectionError(error: String)
}

// Client State Change Handler
fun interface OnClientStateChangeHandler {
  fun onClientStateChanged(device: Device, connected: Boolean)
}

// Server State Change Handler
fun interface OnServerStateChangeHandler {
  fun onServerStateChanged(started: Boolean)
}

// Auth Request Handler
fun interface OnAuthRequestHandler {
  fun onAuthRequest(client: Device)
}

// Sync Request Handler
fun interface OnSyncRequestHandler {
  fun onSyncRequest(items: List<Pair<String, ByteArray>>)
}

// Client List Change Handler
fun interface OnClientListChangeHandler {
  fun onClientListChanged(clients: List<Device>)
}

// Invalid Packet Handler
fun interface OnInvalidPacketHandler {
  fun onInvalidPacket(code: Int, message: String)
}

// On Host Type Change Handler
fun interface OnHostTypeChangeHandler {
  fun onHostTypeChanged(host: HostType)
}
