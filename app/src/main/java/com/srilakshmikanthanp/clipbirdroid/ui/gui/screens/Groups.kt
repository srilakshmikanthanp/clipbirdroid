package com.srilakshmikanthanp.clipbirdroid.ui.gui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.constant.appHomePage
import com.srilakshmikanthanp.clipbirdroid.constant.appIssuesPage
import com.srilakshmikanthanp.clipbirdroid.constant.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.controller.AppController
import com.srilakshmikanthanp.clipbirdroid.intface.OnClientListChangeHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnServerListChangeHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnServerStateChangeHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnServerStatusChangeHandler
import com.srilakshmikanthanp.clipbirdroid.types.device.Device
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.ClipbirdBar
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.DeviceActionable
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.GTab
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.Group
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.HostAction
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.HostList
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.StatusType
import com.srilakshmikanthanp.clipbirdroid.utility.functions.generateX509Certificate

/**
 * Server Group Composable That gonna active when user Clicks Server Tab
 */
@Composable
private fun ServerGroup(controller: AppController) {
  // list of client devices
  var clients by remember { mutableStateOf<List<DeviceActionable>>(emptyList()) }

  // Change Handler for the list of clients
  val clientListChangeHandler = OnClientListChangeHandler { list ->
    clients = list.map { DeviceActionable(it, HostAction.DISCONNECT) }
  }

  // Setup the Server
  val setupServer = {
    controller.addClientListChangedHandler(clientListChangeHandler)
  }

  // dispose the Server
  val disposeServer = {
    controller.removeClientListChangedHandler(clientListChangeHandler)
  }

  // Setup & Dispose the Server
  DisposableEffect(clients) {
    setupServer(); onDispose(disposeServer)
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
private fun ClientGroup(controller: AppController) {
  // list of client devices (state)
  var servers by remember { mutableStateOf<List<DeviceActionable>>(emptyList()) }

  // Change Handler for the list of servers
  val serverListChangeHandler = OnServerListChangeHandler { list ->
    servers = list.map {
      val server = try { controller.getConnectedServer() } catch (e: RuntimeException) { null }
      DeviceActionable(it, if (it == server) HostAction.DISCONNECT else HostAction.CONNECT)
    }
  }

  // Change Handler for Server Status
  val serverStatusChangeHandler = OnServerStatusChangeHandler {
    serverListChangeHandler.onServerListChanged((controller.getServerList()))
  }

  // Action Handler
  val actionHandler = { device: Device, action: HostAction ->
    if (action == HostAction.CONNECT) controller.connectToServer(device)
    else controller.disconnectFromServer(device)
  }

  // set up the Client
  val setupClient = {
    controller.addServerStatusChangedHandler(serverStatusChangeHandler)
    controller.addServerListChangedHandler(serverListChangeHandler)
  }

  // dispose the Client
  val disposeClient = {
    controller.removeServerListChangedHandler(serverListChangeHandler)
    controller.removeServerStatusChangedHandler(serverStatusChangeHandler)
  }

  // Setup & Dispose the Client
  DisposableEffect(servers) {
    setupClient(); onDispose(disposeClient)
  }

  // HostList
  HostList(
    onAction = { actionHandler(it.first, it.second) },
    devicesActionable = servers,
    modifier = Modifier.fillMaxWidth()
  )
}

/**
 * Drop Down Menu used for more actions
 */
@Composable
private fun ActionsDropDownMenu(
  isServerGroup: Boolean = true,
  onQRCodeClick: () -> Unit,
  onJoinClick: () -> Unit,
  onResetClick: () -> Unit,
  onAboutClick: () -> Unit,
  onIssueClick: () -> Unit,
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
) {
  DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest, modifier = modifier) {
    // Conditionally show the menu items
    if (isServerGroup) DropdownMenuItem(text = { Text("Group QrCode") }, onClick = onQRCodeClick)
    else DropdownMenuItem(text = { Text("Join Group") }, onClick = onJoinClick)

    // Show the common menu items
    DropdownMenuItem(text = { Text("Reset") }, onClick = onResetClick)
    DropdownMenuItem(text = { Text("About") }, onClick = onAboutClick)
    DropdownMenuItem(text = { Text("Report Issue") }, onClick = onIssueClick)
  }
}

/**
 * Groups Composable That manage the Server & Client Groups
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Groups(controller: AppController, onMenuClick: () -> Unit = {}, ) {
  // is the Host is lastly server or client
  var isServer by remember { mutableStateOf(controller.isLastlyHostIsServer()) }

  // Expanded state for the Drop Down Menu
  var expanded by remember { mutableStateOf(false) }

  // get the Context instance from compositionLocal
  val context = LocalContext.current

  // initial host name
  val inferHostName = { if (isServer) appMdnsServiceName(context) else "Join to a Group" }

  // initial Status
  val inferStatus = { if (isServer) StatusType.CONNECTED else StatusType.DISCONNECTED }

  // status of the Host
  var status by remember { mutableStateOf(inferStatus()) }

  // Host Name
  var hostName by remember { mutableStateOf(inferHostName()) }

  // Server Tab Index
  val SERVER_TAB = Pair(0, "Create Group")

  // Client Tab Index
  val CLIENT_TAB = Pair(1, "Join Group")

  // Handler for Server Status
  val serverStatusChangeHandler = OnServerStatusChangeHandler {
    hostName = if (it) controller.getConnectedServer().name else "Join to a Group"
    status   = if (it) StatusType.CONNECTED else StatusType.DISCONNECTED
  }

  // Handler for Server State
  val serverStateChangeHandler = OnServerStateChangeHandler {
    hostName = if (it) controller.getServerInfo().name else appMdnsServiceName(context)
    status   = if (it) StatusType.ACTIVE else StatusType.INACTIVE
  }

  // Handler for Tab Click Event
  val tabClickHandler = { index: Int ->
    isServer = index == SERVER_TAB.first
  }

  // Handler for Group QrCode Click Event
  val onQRCodeClick = {
    // TODO: Show QrCode
  }

  // Handler for Join Group Click Event
  val onJoinClick = {
    // TODO: Join Group
  }

  // Handler for Reset Click Event
  val onResetClick = {
    controller.clearClientCertificates()
    controller.clearServerCertificates()
  }

  // Handler for About Click Event
  val onAboutClick = {
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
    intent.data = android.net.Uri.parse(appHomePage())
    context.startActivity(intent)
  }

  // Handler for Issue Click Event
  val onIssueClick = {
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
    intent.data = android.net.Uri.parse(appIssuesPage())
    context.startActivity(intent)
  }

  // Set up lambda
  val setup = {
    // Add Change Handler for the server status
    controller.addServerStatusChangedHandler(serverStatusChangeHandler)

    // Add Change Handler for the server state
    controller.addServerStateChangedHandler(serverStateChangeHandler)

    // initialize the Host as Server or Client
    if (isServer) {
      controller.setCurrentHostAsServer()
    } else {
      controller.setCurrentHostAsClient()
    }

    // Set the Initial HostName & Status for Group
    hostName = inferHostName(); status = inferStatus()
  }

  // Dispose lambda
  val dispose = {
    // Remove Change Handler for the server status
    controller.removeServerStatusChangedHandler(serverStatusChangeHandler)

    // Remove Change Handler for the server state
    controller.removeServerStateChangedHandler(serverStateChangeHandler)
  }

  // Setup & Dispose
  DisposableEffect (isServer) {
    setup(); onDispose(dispose)
  }

  // Actions Drop Down Menu
  val actionsDropDownMenu = @Composable  {
    Box {
      IconButton(onClick = { expanded = true }) {
        Image(painter = painterResource(R.drawable.more), contentDescription = "More",)
      }

      ActionsDropDownMenu(
        onQRCodeClick = { expanded = false; onQRCodeClick()},
        onJoinClick = { expanded = false; onJoinClick()},
        onResetClick = { expanded = false; onResetClick()},
        onAboutClick = { expanded = false; onAboutClick()},
        onIssueClick = { expanded = false; onIssueClick()},
        expanded = expanded,
        isServerGroup = isServer,
        onDismissRequest = { expanded = false }
      )
    }
  }

  // A Inner Composable just to make code more readable
  val groupsTopBar = @Composable {
    ElevatedCard {
      Column {
        // Top Bar for Navigation & Actions
        ClipbirdBar(
          title = { Text("ClipBird Devices", modifier = Modifier.padding(horizontal = 10.dp)) },
          modifier = Modifier.padding(3.dp),
          onMenuClick = onMenuClick,
          actions = { actionsDropDownMenu() }
        )

        // Server Status 40% of parent
        Group(
          modifier = Modifier.fillMaxHeight(0.25f).fillMaxWidth(),
          fontSize = 20.sp,
          status = status,
          hostName = hostName,
        )

        // Tab of Server & Client
        GTab(
          selectedTab = if (isServer) SERVER_TAB.first else CLIENT_TAB.first,
          tabs = listOf(SERVER_TAB.second, CLIENT_TAB.second),
          onTabClick = tabClickHandler
        )
      }
    }
  }

  // Content Composable
  val content = @Composable { padding : PaddingValues ->
    Box (Modifier.padding(padding)) {
      if (isServer) ServerGroup(controller) else ClientGroup(controller)
    }
  }

  // Scaffold Composable
  Scaffold (
    topBar = groupsTopBar,
    content = content,
  )
}

/**
 * Preview for Groups Composable
 */
@Preview(showBackground = true)
@Composable
private fun GroupsPreview() {
  Groups(AppController(generateX509Certificate(LocalContext.current), LocalContext.current))
}
