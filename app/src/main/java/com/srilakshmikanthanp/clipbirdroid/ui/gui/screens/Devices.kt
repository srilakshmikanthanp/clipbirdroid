package com.srilakshmikanthanp.clipbirdroid.ui.gui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.smallTopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.constant.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.controller.AppController
import com.srilakshmikanthanp.clipbirdroid.intface.OnClientListChangeHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnServerListChangeHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnServerStateChangeHandler
import com.srilakshmikanthanp.clipbirdroid.intface.OnServerStatusChangeHandler
import com.srilakshmikanthanp.clipbirdroid.types.device.Device
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.DeviceActionable
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.HostAction
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.HostList
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.Status
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.StatusType
import com.srilakshmikanthanp.clipbirdroid.ui.gui.modals.Connect
import com.srilakshmikanthanp.clipbirdroid.ui.gui.modals.Group
import com.srilakshmikanthanp.clipbirdroid.utility.functions.generateX509Certificate
import com.srilakshmikanthanp.clipbirdroid.utility.functions.getAllInterfaceAddresses
import org.json.JSONObject
import java.net.InetAddress

/**
 * Server Group Composable That gonna active when user Clicks Server Tab
 */
@Composable
private fun ServerGroup(controller: AppController) {
  // list of client devices
  var clients by remember { mutableStateOf(controller.getConnectedClientsList().map {
    DeviceActionable(it, HostAction.DISCONNECT) }
  ) }

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
  var servers by remember { mutableStateOf(controller.getServerList().map {
    val server = try { controller.getConnectedServer() } catch (e: RuntimeException) { null }
    DeviceActionable(it, if (it == server) HostAction.DISCONNECT else HostAction.CONNECT)
  })}

  // Change Handler for the list of servers
  val serverListChangeHandler = OnServerListChangeHandler { list ->
    // get the current server if connected
    val server = if (controller.isConnectedToServer()) {
      controller.getConnectedServer()
    } else {
      null
    }

    // map the list of servers to DeviceActionable
    servers = list.map {
      DeviceActionable(it, if (it == server) HostAction.DISCONNECT else HostAction.CONNECT)
    }

    // if the server is not in the list
    if (server != null && !list.contains(server)) {
      servers = servers + DeviceActionable(server, HostAction.DISCONNECT)
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
  expanded: Boolean,
  modifier: Modifier = Modifier,
  onDismissRequest: () -> Unit,
) {
  DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest, modifier = modifier) {
    // Conditionally show the menu items
    if (isServerGroup) DropdownMenuItem(text = { Text("Group QrCode") }, onClick = onQRCodeClick)
    else DropdownMenuItem(text = { Text("Join Group") }, onClick = onJoinClick)

    // Show the common menu items
    DropdownMenuItem(text = { Text("Reset") }, onClick = onResetClick)
  }
}

/**
 * Groups Composable That manage the Server & Client Groups
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Devices(controller: AppController, onMenuClick: () -> Unit = {}) {
  // is the Host is lastly server or client
  var isServer by remember { mutableStateOf(controller.isCurrentHostIsServer()) }

  // Expanded state for the Drop Down Menu
  var expanded by remember { mutableStateOf(false) }

  // is connect dialog open
  var isConnectDialogOpen by remember { mutableStateOf(false) }

  // is group dialog open
  var isGroupDialogOpen by remember { mutableStateOf(false) }

  // get the Context instance from compositionLocal
  val context = LocalContext.current

  // infer client status
  val inferClientStatus = {
    if (controller.isConnectedToServer()) {
      StatusType.CONNECTED
    } else {
      StatusType.DISCONNECTED
    }
  }

  // infer server status
  val inferServerStatus = {
    if (controller.isServerStarted()) {
      StatusType.ACTIVE
    } else {
      StatusType.INACTIVE
    }
  }

  // initial Status
  val inferStatus = {
    if (isServer) {
      inferServerStatus()
    } else {
      inferClientStatus()
    }
  }

  // infer group name
  val inferGroupName = {
    if(controller.isConnectedToServer()) {
      controller.getConnectedServer().name
    } else {
      "Join to a Group"
    }
  }

  // infer server name
  val inferServerName = {
    if(controller.isServerStarted()) {
      controller.getServerInfo().name
    } else {
      appMdnsServiceName(context)
    }
  }

  // initial host name
  val inferHostName = {
    if (isServer) {
      inferServerName()
    } else {
      inferGroupName()
    }
  }

  // Helper to make json
  val makeJson = {
    val interfaces = getAllInterfaceAddresses()
    val port = controller.getServerInfo().port
    val obj = JSONObject()
    obj.put("ips", interfaces)
    obj.put("port", port)
    obj.toString()
  }

  // Host Name of the Group
  var hostName by remember { mutableStateOf(inferHostName()) }

  // status of the Host
  var status by remember { mutableStateOf(inferStatus()) }

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
    isServer = if (index == SERVER_TAB.first) {
      controller.setCurrentHostAsServer()
      true
    } else {
      controller.setCurrentHostAsClient()
      false
    }
  }

  // on Connect Handler
  val onConnect = { ip: InetAddress, port: Int ->
    controller.connectToServer(Device(ip, port, ip.hostName)).also { isConnectDialogOpen = false }
  }

  // Handler for Group QrCode Click Event
  val onQRCodeClick = {
    isGroupDialogOpen = true
  }

  // Handler for Join Group Click Event
  val onJoinClick = {
    isConnectDialogOpen = true
  }

  // Handler for Reset Click Event
  val onResetClick = {
    controller.clearClientCertificates()
    controller.clearServerCertificates()
  }

  // Set up lambda
  val setup = {
    // Add Change Handler for the server status
    controller.addServerStatusChangedHandler(serverStatusChangeHandler)

    // Add Change Handler for the server state
    controller.addServerStateChangedHandler(serverStateChangeHandler)

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
      // More Icon Image Icon
      IconButton(onClick = { expanded = true }) {
        Image(painter = painterResource(R.drawable.more), contentDescription = "More",)
      }

      // Content
      ActionsDropDownMenu(
        onQRCodeClick = { expanded = false; onQRCodeClick()},
        onJoinClick = { expanded = false; onJoinClick()},
        onResetClick = { expanded = false; onResetClick()},
        expanded = expanded,
        isServerGroup = isServer,
        onDismissRequest = { expanded = false }
      )
    }
  }

  // Menu Icon for the Top Bar
  val menuIcon = @Composable {
    IconButton(onClick = onMenuClick) {
      Image(painter = painterResource(R.drawable.menu), contentDescription = "Menu",)
    }
  }

  // A Inner Composable just to make code more readable
  val devicesTopBar = @Composable {
    Card (shape = RoundedCornerShape(bottomStart = 15.dp, bottomEnd = 15.dp)) {
      Column (horizontalAlignment = Alignment.CenterHorizontally) {
        // Top Bar for Navigation & Actions
        TopAppBar(
          navigationIcon = { menuIcon() },
          title = { Text("Clipbird Devices", modifier = Modifier.padding(horizontal = 10.dp)) },
          modifier = Modifier.padding(3.dp),
          actions = { actionsDropDownMenu() },
          colors = smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        )

        // Server Status % of parent
        Status(
          modifier = Modifier
            .fillMaxHeight(0.15f)
            .fillMaxWidth(),
          fontSize = 24.sp,
          status = status,
          hostName = hostName,
        )

        // Tab of Server & Client
        val selectedTab = if (isServer) SERVER_TAB.first else CLIENT_TAB.first
        val tabs = listOf(SERVER_TAB.second, CLIENT_TAB.second)
        val tabIndex = if (isServer) SERVER_TAB.first else CLIENT_TAB.first

        // Compose the Tab Row
        TabRow(selectedTabIndex = tabIndex, containerColor = Color.Transparent, divider = { }) {
          tabs.forEachIndexed { index, title ->
            Tab(onClick = { tabClickHandler(index) }, selected = (selectedTab == index)) {
              Text(text = title, fontSize = 16.sp, modifier = Modifier.padding(16.dp))
            }
          }
        }
      }
    }
  }

  // Content Composable
  val content = @Composable { padding : PaddingValues ->
    Box (Modifier.padding(padding)) {
      if (isServer) ServerGroup(controller) else ClientGroup(controller)
    }

    if(isGroupDialogOpen) Group(
      onDismissRequest = { isGroupDialogOpen = false },
      title = "Group",
      code = makeJson(),
      port = controller.getServerInfo().port,
      modifier = Modifier
        .padding(top = 20.dp)
        .padding(15.dp)
    )

    if(isConnectDialogOpen) Connect(
      onDismissRequest = { isConnectDialogOpen = false},
      onConnect = onConnect,
      title = "Join Group",
      modifier = Modifier
        .padding(top = 20.dp)
        .padding(15.dp)
    )
  }

  // Scaffold Composable
  Scaffold (
    topBar = devicesTopBar,
    content = content,
  )
}

/**
 * Preview for Groups Composable
 */
@Preview(showBackground = true)
@Composable
private fun DevicesPreview() {
  Devices(AppController(generateX509Certificate(LocalContext.current), LocalContext.current))
}
