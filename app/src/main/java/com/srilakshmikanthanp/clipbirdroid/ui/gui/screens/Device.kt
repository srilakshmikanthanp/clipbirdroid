package com.srilakshmikanthanp.clipbirdroid.ui.gui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.common.enums.HostType
import com.srilakshmikanthanp.clipbirdroid.common.types.Device
import com.srilakshmikanthanp.clipbirdroid.constants.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.controller.AppController
import com.srilakshmikanthanp.clipbirdroid.syncing.lan.client.Client
import com.srilakshmikanthanp.clipbirdroid.syncing.lan.server.Server
import com.srilakshmikanthanp.clipbirdroid.ui.gui.modals.Connect
import com.srilakshmikanthanp.clipbirdroid.ui.gui.modals.Group
import com.srilakshmikanthanp.clipbirdroid.common.functions.generateX509Certificate
import com.srilakshmikanthanp.clipbirdroid.common.functions.getAllInterfaceAddresses
import org.json.JSONObject
import java.net.InetAddress
import java.util.Locale

private typealias ActionableDevice = Pair<Device, HostAction>

private enum class HostAction {
  // Allowed Types connect or disconnect
  CONNECT, DISCONNECT;

  // Enum to String in Capitalize
  override fun toString(): String {
    return super.toString().lowercase(Locale.getDefault()).replaceFirstChar {
      it.titlecase(Locale.getDefault())
    }
  }
}

@Composable
private fun ServerGroup(controller: AppController) {
  // list of client devices
  var clients by remember {
    mutableStateOf(
      controller.getConnectedClientsList().map { ActionableDevice(it, HostAction.DISCONNECT) }
    )
  }

  // Change Handler for the list of clients
  val clientListChangeHandler = Server.OnClientListChangeHandler { list ->
    clients = list.map { ActionableDevice(it, HostAction.DISCONNECT) }
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

  // if no servers
  if (clients.isNotEmpty()) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
      items(clients.size) { i ->
        ListItem(
          leadingContent = {
            Icon(
              painter = painterResource(R.drawable.circle_check_solid),
              contentDescription = stringResource(id = R.string.joined),
              modifier = Modifier.width(30.dp)
            )
          },
          headlineContent = {
            Text(
              style = MaterialTheme.typography.labelLarge,
              text = clients[i].first.name,
              modifier = Modifier.padding(start = 10.dp),
            )
          },
          supportingContent = {
            Text(
              style = MaterialTheme.typography.labelMedium,
              text = stringResource(id = R.string.joined),
              modifier = Modifier.padding(start = 10.dp),
            )
          },
          trailingContent = {
            IconButton(onClick = { controller.disconnectClient(clients[i].first) }) {
              Icon(Icons.Default.Clear, contentDescription = stringResource(id = R.string.disconnect))
            }
          },
        )
      }
    }
  } else {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxWidth().fillMaxHeight(),
    ) {
      Image(
        contentDescription = stringResource(id = R.string.devices_server_prompt),
        painter = painterResource(id = R.drawable.devices),
        modifier = Modifier.fillMaxSize(0.3f)
      )

      Text(
        text = stringResource(id = R.string.devices_server_prompt),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge,
      )
    }
  }
}

/**
 * Client Group Composable That gonna active when user Clicks Client Tab
 */
@Composable
private fun ClientGroup(controller: AppController) {
  // list of client devices (state)
  var servers by remember { mutableStateOf(emptyList<ActionableDevice>()) }

  // connected
  var group by remember { mutableStateOf(controller.getConnectedServer()) }

  // Change Handler for the list of servers
  val serverListChangeHandler = Client.OnServerListChangeHandler { groups ->
    val connected = controller.getConnectedServer()
    servers = groups.filter {
      it != connected
    }.map {
      ActionableDevice(it, HostAction.CONNECT)
    }
  }

  // Change Handler for Server Status
  val serverStatusChangeHandler = Client.OnServerStatusChangeHandler { connected, device ->
    val groupsList = controller.getServerList()
    group = if (connected) device else null
    serverListChangeHandler.onServerListChanged(groupsList)
  }

  // Action Handler
  val onAction = { device: Device, action: HostAction ->
    if (action == HostAction.CONNECT) controller.connectToServer(device)
    else controller.disconnectFromServer(device)
  }

  // set up the Client
  val setupClient = {
    serverListChangeHandler.onServerListChanged(controller.getServerList())
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

  Column {
    group?.let { ActionableDevice(it, HostAction.DISCONNECT) }?.let {
      ListItem(
        leadingContent = {
          Icon(
            painter = painterResource(R.drawable.circle_check_solid),
            contentDescription = stringResource(id = R.string.connected),
            modifier = Modifier.width(30.dp)
          )
        },
        headlineContent = {
          Text(
            style = MaterialTheme.typography.labelLarge,
            text = it.first.name,
            modifier = Modifier.padding(start = 10.dp),
          )
        },
        supportingContent = {
          Text(
            text = stringResource(id = R.string.connected),
            modifier = Modifier.padding(start = 10.dp),
            style = MaterialTheme.typography.labelMedium
          )
        },
        trailingContent = {
          IconButton(onClick = { onAction(it.first, it.second) }) {
            Icon(Icons.Default.Clear, contentDescription = stringResource(id = R.string.disconnect))
          }
        }
      )
    }

    if (servers.isNotEmpty()) {
      LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(servers.size) { i ->
          ListItem(
            leadingContent = {
              Icon(
                painter = painterResource(R.drawable.circle_dot_regular),
                contentDescription = stringResource(id = R.string.available),
                modifier = Modifier.width(30.dp)
              )
            },
            headlineContent = {
              Text(
                style = MaterialTheme.typography.labelLarge,
                text = servers[i].first.name,
                modifier = Modifier.padding(start = 10.dp),
              )
            },
            supportingContent = {
              Text(
                style = MaterialTheme.typography.labelMedium,
                text = stringResource(id = R.string.available),
                modifier = Modifier.padding(start = 10.dp),
              )
            },
            trailingContent = {
              IconButton(onClick = { onAction(servers[i].first, servers[i].second) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.join))
              }
            },
          )
        }
      }
    }

    if (servers.isEmpty() && group == null) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
      ) {
        Image(
          contentDescription = stringResource(id = R.string.devices_client_prompt),
          painter = painterResource(id = R.drawable.devices),
          modifier = Modifier.fillMaxSize(0.3f)
        )

        Text(
          text = stringResource(id = R.string.devices_client_prompt),
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 8.dp)
        )
      }
    }
  }
}

/**
 * Drop Down Menu used for more actions
 */
@Composable
private fun ActionsDropDownMenu(
  modifier: Modifier = Modifier,
  isServerGroup: Boolean = true,
  onQRCodeClick: () -> Unit,
  onJoinClick: () -> Unit,
  onResetClick: () -> Unit,
  expanded: Boolean,
  onDismissRequest: () -> Unit,
) {
  DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest, modifier = modifier) {
    if (isServerGroup) {
      DropdownMenuItem(
        text = { Text(stringResource(id = R.string.group_qrcode)) },
        onClick = onQRCodeClick
      )
    } else {
      DropdownMenuItem(
        text = { Text(stringResource(id = R.string.join_to_a_group)) },
        onClick = onJoinClick
      )
    }.also {
      DropdownMenuItem(
        text = { Text(stringResource(id = R.string.reset_devices)) },
        onClick = onResetClick
      )
    }
  }
}

/**
 * Groups Composable That manage the Server & Client Groups
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Devices(controller: AppController, onMenuClick: () -> Unit = {}) {
  var isServer by remember { mutableStateOf(controller.getHostType() == HostType.SERVER) }
  var expanded by remember { mutableStateOf(false) }
  var isConnectDialogOpen by remember { mutableStateOf(false) }
  var isGroupDialogOpen by remember { mutableStateOf(false) }

  val context = LocalContext.current

  // Helper to make json
  val makeJson = {
    val interfaces = getAllInterfaceAddresses()
    val port = controller.getServerInfo().port
    val obj = JSONObject()
    obj.put("port", port)
    obj.put("ips", interfaces)
    obj.toString(0)
  }

  // Server Tab Index
  val serverTab = Pair(0, context.resources.getString(R.string.create_group))

  // Client Tab Index
  val clintTab = Pair(1, context.resources.getString(R.string.join_group))

  // Handler for Tab Click Event
  val tabClickHandler = { index: Int ->
    isServer = if (index == serverTab.first && !isServer) {
      controller.setCurrentHostAsServer()
      true
    } else if (index == clintTab.first && isServer) {
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

  val handleServerStart = Server.OnMdnsRegisterStatusChangeHandler {
    if (it) { Toast.makeText(context, "Registered", Toast.LENGTH_SHORT).show() }
  }

  val setup = {
    controller.addServerStateChangedHandler(handleServerStart)
  }

  val dispose = {
    controller.removeServerStateChangedHandler(handleServerStart)
  }

  DisposableEffect(Unit) {
    setup()
    onDispose { dispose() }
  }

  // Actions Drop Down Menu
  val actionsDropDownMenu = @Composable {
    Box {
      IconButton(onClick = { expanded = true }) {
        Image(
          painter = painterResource(R.drawable.more),
          contentDescription = context.resources.getString(R.string.more),
        )
      }

      ActionsDropDownMenu(
        onQRCodeClick = { expanded = false; onQRCodeClick() },
        onJoinClick = { expanded = false; onJoinClick() },
        onResetClick = { expanded = false; onResetClick() },
        expanded = expanded,
        isServerGroup = isServer,
        onDismissRequest = { expanded = false }
      )
    }
  }

  // Menu Icon for the Top Bar
  val menuIcon = @Composable {
    IconButton(onClick = onMenuClick) {
      Image(
        painter = painterResource(R.drawable.menu),
        contentDescription = context.resources.getString(R.string.menu),
      )
    }
  }

  // A Inner Composable just to make code more readable
  val devicesTopBar = @Composable {
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 4.dp,
      shadowElevation = 4.dp,
    ) {
      Column {
        TopAppBar(
          title = {
            Text(
              context.resources.getString(R.string.clipbird_devices),
              modifier = Modifier.padding(horizontal = 10.dp)
            )
          },
          navigationIcon = { menuIcon() },
          actions = { actionsDropDownMenu() },
        )

        Spacer(modifier = Modifier.height(10.dp))

        val selectedTab = if (isServer) serverTab.first else clintTab.first
        val tabs = listOf(serverTab.second, clintTab.second)
        val tabIndex = if (isServer) serverTab.first else clintTab.first

        TabRow(
          selectedTabIndex = tabIndex,
        ) {
          tabs.forEachIndexed { index, title ->
            Tab(
              selected = selectedTab == index,
              onClick = { tabClickHandler(index) },
              text = {
                Text(text = title)
              }
            )
          }
        }
      }
    }
  }

  // Content Composable
  val content = @Composable { padding: PaddingValues ->
    Column(modifier = Modifier.padding(padding)) {
      if (isServer) {
        ServerGroup(controller)
      } else {
        ClientGroup(controller)
      }
    }

    if (isGroupDialogOpen) Group(
      onDismissRequest = { isGroupDialogOpen = false },
      title = appMdnsServiceName(context),
      code = makeJson(),
      port = controller.getServerInfo().port,
      modifier = Modifier.padding(5.dp, 25.dp, 5.dp, 15.dp)
    )

    if (isConnectDialogOpen) Connect(
      onDismissRequest = { isConnectDialogOpen = false },
      onConnect = onConnect,
      title = context.resources.getString(R.string.join_group),
      modifier = Modifier.padding(15.dp, 25.dp)
    )
  }

  // Scaffold Composable
  Scaffold(
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
