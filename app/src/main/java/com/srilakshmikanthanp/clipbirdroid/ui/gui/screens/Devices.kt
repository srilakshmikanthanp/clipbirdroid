package com.srilakshmikanthanp.clipbirdroid.ui.gui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.res.stringResource
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
import com.srilakshmikanthanp.clipbirdroid.types.enums.HostType
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.DeviceActionable
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.Host
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.HostAction
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
  var clients by remember { mutableStateOf(
    controller.getConnectedClientsList().map { DeviceActionable(it, HostAction.DISCONNECT) }
  )}

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
  Card (modifier = Modifier.padding(15.dp, 15.dp, 15.dp, 15.dp)) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
      items(clients.size) { i ->
        val modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 5.dp)
        val onAction = { d: DeviceActionable -> controller.disconnectClient(d.first) }
        Host(clients[i], modifier, onAction)
      }
    }
  }

  // if no servers
  if (clients.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(text = stringResource(id = R.string.nothing_there), fontSize = 16.sp, color = Color.Gray)
    }
  }
}

/**
 * Client Group Composable That gonna active when user Clicks Client Tab
 */
@Composable
private fun ClientGroup(controller: AppController) {
  // list of client devices (state)
  var servers by remember { mutableStateOf(emptyList<DeviceActionable>()) }

  // connected
  var group by remember { mutableStateOf(controller.getConnectedServer()) }

  // Change Handler for the list of servers
  val serverListChangeHandler = OnServerListChangeHandler { list ->
    // get connected server
    val connected = controller.getConnectedServer()

    // update servers
    servers = list.filter {
      it != connected
    }.map {
      DeviceActionable(it, HostAction.CONNECT)
    }
  }

  // Change Handler for Server Status
  val serverStatusChangeHandler = OnServerStatusChangeHandler { connected, device ->
    val groupsList = controller.getServerList()
    group = if(connected) device else null
    serverListChangeHandler.onServerListChanged(groupsList)
  }

  // Action Handler
  val onAction = { device: Device, action: HostAction ->
    if (action == HostAction.CONNECT) controller.connectToServer(device)
    else controller.disconnectFromServer(device)
  }

  // set up the Client
  val setupClient = {
    // manually trigger the server list change handler
    serverListChangeHandler.onServerListChanged(controller.getServerList())

    // Add Change Handler for the server status & list
    controller.addServerStatusChangedHandler(serverStatusChangeHandler)
    controller.addServerListChangedHandler(serverListChangeHandler)
  }

  // dispose the Client
  val disposeClient = {
    controller.removeServerListChangedHandler(serverListChangeHandler)
    controller.removeServerStatusChangedHandler(serverStatusChangeHandler)
  }

  // always make connected server on top
  servers = servers.sortedBy { it.second != HostAction.DISCONNECT }

  // Setup & Dispose the Client
  DisposableEffect(servers) {
    setupClient(); onDispose(disposeClient)
  }

  // Modifier for the Host
  val modifier = Modifier
    .fillMaxWidth()
    .padding(horizontal = 5.dp)

  // Render the view
  Column {
    // Current Group is not null
    if (group != null) {
      Text(
        text = stringResource(id = R.string.current_group),
        fontSize = 16.sp, color = Color.Gray,
        modifier = Modifier.padding(15.dp)
      )
    }

    // if has connected server
    group?.let { DeviceActionable(it, HostAction.DISCONNECT) } ?.let {
      Card (modifier = Modifier.padding(horizontal = 15.dp)) {
        Host(it, modifier, onAction =  { onAction(it.first, it.second) })
      }
    }

    // Other Groups
    if (!servers.isEmpty()) {
      Text(
        text = stringResource(id = R.string.other_groups),
        fontSize = 16.sp, color = Color.Gray,
        modifier = Modifier.padding(15.dp)
      )
    }

    // List of Hosts
    Card (modifier = Modifier.padding(15.dp, 0.dp, 15.dp, 15.dp)) {
      // Render the Remaining Groups
      LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(servers.size) { i ->
          Host(servers[i], modifier, onAction =  { onAction(it.first, it.second) })
        }
      }
    }

    // if no servers
    if (servers.isEmpty() && group == null) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = stringResource(id = R.string.nothing_there), fontSize = 16.sp, color = Color.Gray)
      }
    }
  }
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
    if (isServerGroup) {
      DropdownMenuItem(text = { Text(stringResource(id = R.string.group_qrcode)) }, onClick = onQRCodeClick)
    } else {
      DropdownMenuItem(text = { Text(stringResource(id = R.string.join_to_a_group)) }, onClick = onJoinClick)
    }.also {
      DropdownMenuItem(text = { Text(stringResource(id = R.string.reset_devices)) }, onClick = onResetClick)
    }
  }
}

/**
 * Groups Composable That manage the Server & Client Groups
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Devices(controller: AppController, onMenuClick: () -> Unit = {}) {
  // is the Host is lastly server or client
  var isServer by remember { mutableStateOf(controller.getHostType() == HostType.SERVER) }

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
    if (controller.getConnectedServer() != null) {
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
    controller.getConnectedServer()?.name ?: context.resources.getString(R.string.not_connected)
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
    obj.put("port", port)
    obj.put("ips", interfaces)
    obj.toString(0)
  }

  // Host Name of the Group
  var hostName by remember { mutableStateOf(inferHostName()) }

  // status of the Host
  var status by remember { mutableStateOf(inferStatus()) }

  // Server Tab Index
  val SERVER_TAB = Pair(0, context.resources.getString(R.string.create_group))

  // Client Tab Index
  val CLIENT_TAB = Pair(1,context.resources.getString(R.string.join_group))

  // Handler for Server Status
  val serverStatusChangeHandler = OnServerStatusChangeHandler { s, d ->
    hostName = if (s) d.name else context.resources.getString(R.string.not_connected)
    status   = if (s) StatusType.CONNECTED else StatusType.DISCONNECTED
  }

  // Handler for Server State
  val serverStateChangeHandler = OnServerStateChangeHandler {
    hostName = if (it) controller.getServerInfo().name else appMdnsServiceName(context)
    status   = if (it) StatusType.ACTIVE else StatusType.INACTIVE
  }

  // Handler for Tab Click Event
  val tabClickHandler = { index: Int ->
    isServer = if (index == SERVER_TAB.first && !isServer) {
      controller.setCurrentHostAsServer()
      true
    } else if (index == CLIENT_TAB.first && isServer) {
      controller.setCurrentHostAsClient()
      false
    } else {
      isServer
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
        Image(painter = painterResource(R.drawable.more), contentDescription = context.resources.getString(R.string.more),)
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
      Image(painter = painterResource(R.drawable.menu), contentDescription = context.resources.getString(R.string.menu),)
    }
  }

  // A Inner Composable just to make code more readable
  val devicesTopBar = @Composable {
    // Top Bar for Navigation & Actions
    TopAppBar(
      navigationIcon = { menuIcon() },
      title = { Text(context.resources.getString(R.string.clipbird_devices), modifier = Modifier.padding(horizontal = 10.dp)) },
      modifier = Modifier.padding(3.dp),
      actions = { actionsDropDownMenu() },
    )
  }

  // Content Composable
  val content = @Composable { padding : PaddingValues ->
    Column (modifier = Modifier.padding(padding)) {
      Card (modifier = Modifier.padding(15.dp, 15.dp, 15.dp, 15.dp)) {
        Column (horizontalAlignment = Alignment.CenterHorizontally) {
          // Server Status % of parent
          Status(
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 20.dp),
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

      // Show the selected group
      if (isServer) {
        ServerGroup(controller)
      } else {
        ClientGroup(controller)
      }
    }

    if(isGroupDialogOpen) Group(
      onDismissRequest = { isGroupDialogOpen = false },
      title = appMdnsServiceName(context),
      code = makeJson(),
      port = controller.getServerInfo().port,
      modifier = Modifier.padding(5.dp, 25.dp, 5.dp, 15.dp)
    )

    if(isConnectDialogOpen) Connect(
      onDismissRequest = { isConnectDialogOpen = false},
      onConnect = onConnect,
      title = context.resources.getString(R.string.join_group),
      modifier = Modifier.padding(15.dp, 25.dp)
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
