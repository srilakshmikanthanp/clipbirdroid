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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.common.enums.HostType
import com.srilakshmikanthanp.clipbirdroid.common.functions.getAllInterfaceAddresses
import com.srilakshmikanthanp.clipbirdroid.common.types.Device
import com.srilakshmikanthanp.clipbirdroid.constants.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.storage.Storage
import com.srilakshmikanthanp.clipbirdroid.syncing.lan.Server
import com.srilakshmikanthanp.clipbirdroid.controller.ControllerViewModel
import com.srilakshmikanthanp.clipbirdroid.ui.gui.modals.Connect
import com.srilakshmikanthanp.clipbirdroid.ui.gui.modals.Group
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.net.InetAddress

@Composable
private fun ServerGroup(
  controllerViewModel: ControllerViewModel = hiltViewModel<ControllerViewModel>()
) {
  val isRegistered by controllerViewModel.lanController.mdnsRegisterStatusEvents.collectAsState(false)
  val clients by controllerViewModel.lanController.clients.collectAsState()

  val context = LocalContext.current

  if (isRegistered) Toast.makeText(context, "Registered", Toast.LENGTH_SHORT).show()

  val onDisconnectClick = { device: Device ->
    controllerViewModel.lanController.getHostAsServerOrThrow().disconnectClient(device)
  }

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
              text = clients[i].name,
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
            IconButton(onClick = { onDisconnectClick(clients[i]) }) {
              Icon(
                Icons.Default.Clear,
                contentDescription = stringResource(id = R.string.disconnect)
              )
            }
          },
        )
      }
    }
  } else {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(),
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

@Composable
private fun ClientGroup(
  controllerViewModel: ControllerViewModel = hiltViewModel<ControllerViewModel>()
) {
  val connectedGroup by controllerViewModel.lanController.serverStatusEvents.map { if (it.first) it.second else null }.collectAsState(null)
  val servers by controllerViewModel.lanController.servers.map { it.toList() }.collectAsState(emptyList())

  val onDisconnect = { device: Device ->
    controllerViewModel.lanController.getHostAsClientOrThrow().disconnectFromServer()
  }

  val onConnect = { device: Device ->
    controllerViewModel.lanController.getHostAsClientOrThrow().connectToServer(device)
  }

  Column {
    connectedGroup?.let {
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
            text = it.name,
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
          IconButton(onClick = { onDisconnect(it) }) {
            Icon(
              Icons.Default.Clear,
              contentDescription = stringResource(id = R.string.disconnect)
            )
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
                text = servers[i].name,
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
              IconButton(onClick = {
                onConnect(servers[i])
              }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.join))
              }
            },
          )
        }
      }
    }

    if (servers.isEmpty() && connectedGroup == null) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight(),
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
  controllerViewModel: ControllerViewModel = hiltViewModel<ControllerViewModel>(),
  expanded: Boolean = false,
  onDismissRequest: () -> Unit,
) {
  val hostType by controllerViewModel.lanController.hostTypeChangeEvent.collectAsState(controllerViewModel.lanController.getHostType())
  val hubState by controllerViewModel.wanController.hubConnectionStatus.collectAsState(false)

  var isConnectDialogOpen by remember { mutableStateOf(false) }
  var isGroupDialogOpen by remember { mutableStateOf(false) }

  val context = LocalContext.current

  val hubHostDevice by Storage.getInstance(context).hubHostDeviceFlow.collectAsState()
  val hubAuthToken by Storage.getInstance(context).hubAuthTokenFlow.collectAsState()

  val makeJson = {
    val interfaces = getAllInterfaceAddresses()
    val port = controllerViewModel.lanController.getHostAsServerOrThrow().getServerInfo().port
    val obj = JSONObject()
    obj.put("port", port)
    obj.put("ips", interfaces)
    obj.toString(0)
  }

  val onQRCodeClick = {
    isGroupDialogOpen = true
    onDismissRequest()
  }

  val onJoinClick = {
    isConnectDialogOpen = true
    onDismissRequest()
  }

  val onConnect = { ip: InetAddress, port: Int ->
    controllerViewModel.lanController.getHostAsClientOrThrow().connectToServer(Device(ip, port, ip.hostName))
    isConnectDialogOpen = false
    onDismissRequest()
  }

  val onResetClick = {
    Storage.getInstance(context).clearAllClientCert()
    Storage.getInstance(context).clearAllServerCert()
    Toast.makeText(context, R.string.reset_done, Toast.LENGTH_SHORT).show()
    onDismissRequest()
  }

  val onLeaveHubClick = {
    controllerViewModel.wanController.disconnectFromHub()
    onDismissRequest()
  }

  val onJoinHubClick = {
    controllerViewModel.wanController.connectToHub(hubHostDevice!!)
    onDismissRequest()
  }

  DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest, modifier = modifier) {
    if (hostType == HostType.SERVER) {
      DropdownMenuItem(
        text = { Text(stringResource(id = R.string.group_qrcode)) },
        onClick = onQRCodeClick
      )
    }

    if (hostType == HostType.CLIENT) {
      DropdownMenuItem(
        text = { Text(stringResource(id = R.string.join_to_a_group)) },
        onClick = onJoinClick
      )
    }

    if (hubAuthToken != null && hubHostDevice != null && !hubState) {
      DropdownMenuItem(
        text = { Text(stringResource(id = R.string.join_hub)) },
        onClick = onJoinHubClick,
      )
    }

    if (hubState) {
      DropdownMenuItem(
        text = { Text(stringResource(id = R.string.leave_hub)) },
        onClick = onLeaveHubClick,
      )
    }

    DropdownMenuItem(
      text = { Text(stringResource(id = R.string.reset_devices)) },
      onClick = onResetClick
    )
  }

  if (isGroupDialogOpen) Group(
    onDismissRequest = { isGroupDialogOpen = false },
    title = appMdnsServiceName(context),
    code = makeJson(),
    port = controllerViewModel.lanController.getHostAsServerOrThrow().getServerInfo().port,
    modifier = Modifier.padding(5.dp, 25.dp, 5.dp, 15.dp)
  )

  if (isConnectDialogOpen) Connect(
    onDismissRequest = { isConnectDialogOpen = false },
    onConnect = onConnect,
    title = stringResource(R.string.join_group),
    modifier = Modifier.padding(15.dp, 25.dp)
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Devices(
  controllerViewModel: ControllerViewModel = hiltViewModel<ControllerViewModel>(),
  onMenuClick: () -> Unit = {}
) {
  var isServer by remember { mutableStateOf(controllerViewModel.lanController.getHost() is Server) }
  var expanded by remember { mutableStateOf(false) }

  val serverTab = Pair(0, stringResource(R.string.create_group))
  val clintTab = Pair(1, stringResource(R.string.join_group))

  val tabClickHandler = { index: Int ->
    isServer = if (index == serverTab.first && !isServer) {
      controllerViewModel.lanController.setAsServer()
      true
    } else if (index == clintTab.first && isServer) {
      controllerViewModel.lanController.setAsClient()
      false
    } else {
      isServer
    }
  }

  val actionsDropDownMenu = @Composable {
    Box {
      IconButton(onClick = { expanded = true }) {
        Image(
          painter = painterResource(R.drawable.more),
          contentDescription = stringResource(R.string.more),
        )
      }

      ActionsDropDownMenu(
        controllerViewModel = controllerViewModel,
        expanded = expanded,
        onDismissRequest = { expanded = false },
      )
    }
  }

  val menuIcon = @Composable {
    IconButton(onClick = onMenuClick) {
      Image(
        painter = painterResource(R.drawable.menu),
        contentDescription = stringResource(R.string.menu),
      )
    }
  }

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
              stringResource(R.string.clipbird_devices),
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

  val content = @Composable { padding: PaddingValues ->
    Column(modifier = Modifier.padding(padding)) {
      if (isServer) {
        ServerGroup(controllerViewModel)
      } else {
        ClientGroup(controllerViewModel)
      }
    }
  }

  Scaffold(
    topBar = devicesTopBar,
    content = content,
  )
}
