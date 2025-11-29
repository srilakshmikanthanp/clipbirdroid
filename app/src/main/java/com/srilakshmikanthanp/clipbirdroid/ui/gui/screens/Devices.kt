package com.srilakshmikanthanp.clipbirdroid.ui.gui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.srilakshmikanthanp.clipbirdroid.ApplicationStateViewModel
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedDevicesViewModel
import com.srilakshmikanthanp.clipbirdroid.syncing.SyncingViewModel
import kotlinx.coroutines.flow.map

@Composable
private fun ServerGroup(
  trustedDevicesViewModel: TrustedDevicesViewModel = hiltViewModel(),
  syncingViewModel: SyncingViewModel = hiltViewModel<SyncingViewModel>()
) {
  val clients by syncingViewModel.syncingManager.connectedClients.collectAsState(initial = emptyList())

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
            val trusted by clients[i].isTrusted.collectAsState()
            Row {
              IconButton(onClick = { syncingViewModel.disconnectClient(clients[i]) }) {
                Icon(
                  Icons.Default.Clear,
                  contentDescription = stringResource(id = R.string.disconnect)
                )
              }
              if (!trusted) {
                IconButton(onClick = { trustedDevicesViewModel.addTrustedClient(clients[i].name, clients[i].getCertificate()) }) {
                  Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add_to_trust_device)
                  )
                }
              }
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
  trustedDevicesViewModel: TrustedDevicesViewModel = hiltViewModel(),
  syncingViewModel: SyncingViewModel = hiltViewModel<SyncingViewModel>(),
) {
  val connected by syncingViewModel.syncingManager.connectedServer.collectAsState(null)
  val servers by syncingViewModel.syncingManager.availableServers.map { list -> list.filter { it.name != connected?.name } }.collectAsState(emptyList())

  Column {
    connected?.let {
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
          Row {
            val trusted by it.isTrusted.collectAsState()
            IconButton(onClick = { syncingViewModel.disconnectClient(it) }) {
              Icon(
                Icons.Default.Clear,
                contentDescription = stringResource(id = R.string.disconnect)
              )
            }
            if (!trusted) {
              IconButton(onClick = { trustedDevicesViewModel.addTrustedServer(it.name, it.getCertificate()) }) {
                Icon(
                  Icons.Default.Add,
                  contentDescription = stringResource(id = R.string.add_to_trust_device)
                )
              }
            }
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
                syncingViewModel.connectToServer(servers[i])
              }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.join))
              }
            },
          )
        }
      }
    }

    if (servers.isEmpty() && connected == null) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Devices(
  applicationStateViewModel: ApplicationStateViewModel = hiltViewModel<ApplicationStateViewModel>(),
  onMenuClick: () -> Unit = {}
) {
  val isServer = applicationStateViewModel.applicationState.isServerFlow.collectAsState().value

  val serverTab = Pair(0, stringResource(R.string.create_group))
  val clintTab = Pair(1, stringResource(R.string.join_group))

  val tabClickHandler = { index: Int ->
    when (index) {
      serverTab.first -> {
        if (!isServer) {
          applicationStateViewModel.applicationState.setIsServer(true)
        }
      }

      clintTab.first -> {
        if (isServer) {
          applicationStateViewModel.applicationState.setIsServer(false)
        }
      }
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

  val appBarSurface = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)

  val devicesTopBar = @Composable {
    Surface(
      color = appBarSurface,
      tonalElevation = 4.dp,
      shadowElevation = 4.dp,
    ) {
      Column {
        TopAppBar(
          colors = TopAppBarDefaults.topAppBarColors(
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
            containerColor = appBarSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
          ),
          title = {
            Text(
              stringResource(R.string.clipbird_devices),
              modifier = Modifier.padding(horizontal = 10.dp)
            )
          },
          navigationIcon = { menuIcon() },
        )

        Spacer(modifier = Modifier.height(10.dp))

        val selectedTab = if (isServer) serverTab.first else clintTab.first
        val tabs = listOf(serverTab.second, clintTab.second)
        val tabIndex = if (isServer) serverTab.first else clintTab.first

        PrimaryTabRow (
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
        ServerGroup()
      } else {
        ClientGroup()
      }
    }
  }

  Scaffold(
    topBar = devicesTopBar,
    content = content,
  )
}
