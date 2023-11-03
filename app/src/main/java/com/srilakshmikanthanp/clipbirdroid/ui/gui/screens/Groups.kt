package com.srilakshmikanthanp.clipbirdroid.ui.gui.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srilakshmikanthanp.clipbirdroid.constant.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.controller.AppController
import com.srilakshmikanthanp.clipbirdroid.intface.OnClientListChangeHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnServerListChangeHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnServerStateChangeHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnServerStatusChangeHandler
import com.srilakshmikanthanp.clipbirdroid.types.device.Device
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.DeviceActionable
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.Group
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.HostAction
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.HostList
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.StatusType

/**
 * Server Group Composable That gonna active when user Clicks Server Tab
 */
@Composable
private fun ServerGroup() {
  // get the Controller instance from compositionLocal
  val controller = compositionLocalOf<AppController> { error("No AppController provided") }.current

  // list of client devices
  var clients by remember { mutableStateOf<List<DeviceActionable>>(emptyList()) }

  // Change Handler for the list of clients
  val clientListChangeHandler = OnClientListChangeHandler { list ->
    clients = list.map { DeviceActionable(it, HostAction.DISCONNECT) }
  }

  // Setup the Server
  val setupServer = {
    // Add Change Handler for the list of clients
    controller.addClientListChangedHandler(clientListChangeHandler)

    // set the Current Host as Server
    controller.setCurrentHostAsServer()
  }

  // dispose the Server
  val disposeServer = {
    // Remove Change Handler for the list of clients
    controller.removeClientListChangedHandler(clientListChangeHandler)

    // Dispose the Server After Un Registering Signals
    controller.disposeServer()
  }

  // Setup & Dispose the Server
  DisposableEffect(Unit) {
    setupServer(); onDispose { disposeServer() }
  }

  // HostList
  HostList(
    onAction = { controller.disconnectClient(it.first) },
    devicesActionable = clients,
    modifier = Modifier.fillMaxWidth()
  )
}

/**
 * Client Group Composable That gonna active when user Clicks Client Tab
 */
@Composable
private fun ClientGroup() {
  // get the Controller instance from compositionLocal
  val controller = compositionLocalOf<AppController> { error("No AppController provided") }.current

  // list of client devices (state)
  var servers by remember { mutableStateOf<List<DeviceActionable>>(emptyList()) }

  // Change Handler for the list of servers
  val serverListChangeHandler = OnServerListChangeHandler { list ->
    servers = list.map {
      val server = try { controller.getConnectedServer() } catch (e: RuntimeException) { null }
      DeviceActionable(it, if (it == server) HostAction.DISCONNECT else HostAction.CONNECT)
    }
  }

  // Action Handler
  val actionHandler = { device: Device, action: HostAction ->
    if (action == HostAction.CONNECT) controller.connectToServer(device)
    else controller.disconnectFromServer(device)
  }

  // set up the Client
  val setupClient = {
    // Add Change Handler for the list of servers
    controller.addServerListChangedHandler(serverListChangeHandler)

    // set the Current Host as Client
    controller.setCurrentHostAsClient()
  }

  // dispose the Client
  val disposeClient = {
    // Remove Change Handler for the list of servers
    controller.removeServerListChangedHandler(serverListChangeHandler)

    // Dispose the Client After Un Registering Signals
    controller.disposeClient()
  }

  // Setup & Dispose the Client
  DisposableEffect(Unit) {
    setupClient(); onDispose { disposeClient() }
  }

  // HostList
  HostList(
    onAction = { actionHandler(it.first, it.second) },
    devicesActionable = servers,
    modifier = Modifier.fillMaxWidth()
  )
}

/**
 * Groups Composable That manage the Server & Client Groups
 */
@Composable
fun Groups() {
  // get the Controller instance from compositionLocal
  val controller = compositionLocalOf<AppController> { error("No AppController provided") }.current

  // get the Context instance from compositionLocal
  val context = compositionLocalOf<Context> { error("No Context provided") }.current

  // is the Host is lastly server or client
  var isServer by remember { mutableStateOf(controller.isLastlyHostIsServer()) }

  // status of the Host
  var status by remember { mutableStateOf(if (isServer) StatusType.INACTIVE else StatusType.DISCONNECTED) }

  // Infer the Host name from available details
  val inferHostName : () -> String = {
    // If the host is client and connected to server
    if (!isServer && controller.isConnectedToServer()) {
      controller.getConnectedServer().name
    }

    // if the host if client and not connected
    else if (!isServer) {
      "Join to a Group"
    }

    // if the host is currently server the return service name
    else {
      appMdnsServiceName(context)
    }
  }

  // Handler for Server Status
  val serverStatusChangeHandler = OnServerStatusChangeHandler {
    status = if (it) StatusType.CONNECTED else StatusType.DISCONNECTED
  }

  // Handler for Server State
  val serverStateChangeHandler = OnServerStateChangeHandler {
    status = if (it) StatusType.ACTIVE else StatusType.INACTIVE
  }

  // Set up lambda
  val setup = {
    // Add Change Handler for the server status
    controller.addServerStatusChangedHandler(serverStatusChangeHandler)

    // Add Change Handler for the server state
    controller.addServerStateChangedHandler(serverStateChangeHandler)

    // if is Server then set current host as server
    if (isServer) controller.setCurrentHostAsServer()

    // if is Client then set current host as client
    if (!isServer) controller.setCurrentHostAsClient()
  }

  // Dispose lambda
  val dispose = {
    // Remove Change Handler for the server status
    controller.removeServerStatusChangedHandler(serverStatusChangeHandler)

    // Remove Change Handler for the server state
    controller.removeServerStateChangedHandler(serverStateChangeHandler)
  }

  // Setup & Dispose
  DisposableEffect(Unit) {
    setup(); onDispose { dispose() }
  }

  // First Show the Server Status and then a tab to switch between Server & Client
  Column {
    // Server Status
    Group(
      modifier = Modifier.padding(vertical = 70.dp).fillMaxWidth(),
      fontSize = 20.sp,
      status = status,
      hostName = inferHostName(),
    )
  }
}
