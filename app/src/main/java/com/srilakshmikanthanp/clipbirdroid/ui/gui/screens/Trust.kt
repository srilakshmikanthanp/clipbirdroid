package com.srilakshmikanthanp.clipbirdroid.ui.gui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedDevicesViewModel
import com.srilakshmikanthanp.clipbirdroid.syncing.Session
import com.srilakshmikanthanp.clipbirdroid.syncing.SyncingViewModel
import java.security.MessageDigest
import java.security.cert.X509Certificate
import kotlin.collections.toList

private fun sha256Fingerprint(cert: X509Certificate): String {
  return MessageDigest.getInstance("SHA-256").digest(cert.encoded).joinToString(":") { "%02X".format(it) }
}

@Composable
private fun EmptyTrustedState(message: String) {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Image(
      painter = painterResource(R.drawable.devices),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(0.3f)
    )
    Text(
      text = message,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center,
      style = MaterialTheme.typography.bodyLarge
    )
  }
}

@Composable
private fun DeviceRow(
  name: String,
  cert: X509Certificate,
  trailing: @Composable () -> Unit
) {
  val fingerprint = remember(cert) { sha256Fingerprint(cert) }
  var showDialog by remember { mutableStateOf(false) }

  if (showDialog) {
    AlertDialog(
      onDismissRequest = { showDialog = false },
      confirmButton = {
        TextButton(onClick = { showDialog = false }) {
          Text("OK")
        }
      },
      title = { Text("Fingerprint") },
      text = {
        Text(
          text = fingerprint,
          style = MaterialTheme.typography.bodyMedium
        )
      }
    )
  }

  ListItem(
    headlineContent = {
      Text(
        text = name,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
      )
    },
    supportingContent = {
      Text(
        text = fingerprint,
        style = MaterialTheme.typography.bodySmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    },
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
      .clickable { showDialog = true },
    trailingContent = trailing
  )
}

@Composable
fun TrustedDeviceRow(
  name: String,
  cert: X509Certificate,
  remove: () -> Unit
) {
  DeviceRow(
    name = name,
    cert = cert,
    trailing = {
      IconButton(onClick = remove) {
        Icon(
          Icons.Default.Delete,
          contentDescription = stringResource(R.string.remove),
          tint = MaterialTheme.colorScheme.error
        )
      }
    }
  )
}

@Composable
private fun TrustedServerList(
  servers: Map<String, X509Certificate>,
  remove: (String) -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(10.dp)
  ) {

    if (servers.isEmpty()) {
      EmptyTrustedState(message = stringResource(R.string.no_trusted_servers))
      return
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(bottom = 10.dp)
    ) {
      items(servers.toList(), key = { it.first }) { (name, cert) ->
        TrustedDeviceRow(
          name = name,
          cert = cert,
          remove = { remove(name) }
        )
      }
    }
  }
}

@Composable
private fun TrustedClientList(
  clients: Map<String, X509Certificate>,
  remove: (String) -> Unit
) {
  if (clients.isEmpty()) {
    EmptyTrustedState(message = stringResource(R.string.no_trusted_servers))
    return
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(10.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    items(clients.toList()) { (name, cert) ->
      TrustedDeviceRow(name = name, cert = cert, remove = { remove(name) })
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrustedDevices(
  trustedDevicesViewModel: TrustedDevicesViewModel = hiltViewModel(),
  onMenuClick: () -> Unit = {}
) {
  val trustedServers by trustedDevicesViewModel.trustedServersFlow.collectAsState()
  val trustedClients by trustedDevicesViewModel.trustedClientsFlow.collectAsState()

  var tabIndex by remember { mutableIntStateOf(0) }

  val tabs = listOf(
    stringResource(R.string.trusted_servers),
    stringResource(R.string.trusted_clients)
  )

  val appBarSurface = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)

  Scaffold(
    topBar = {
      Surface(
        color = appBarSurface,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
      ) {
        Column {
          TopAppBar(
            title = {
              Text(
                stringResource(R.string.trusted_devices),
                modifier = Modifier.padding(horizontal = 10.dp)
              )
            },
            navigationIcon = {
              IconButton(onClick = onMenuClick) {
                Image(
                  painter = painterResource(R.drawable.menu),
                  contentDescription = stringResource(R.string.menu)
                )
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(
              containerColor = appBarSurface
            )
          )

          Spacer(Modifier.height(10.dp))

          PrimaryTabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
              Tab(
                selected = tabIndex == index,
                onClick = { tabIndex = index },
                text = { Text(title) }
              )
            }
          }
        }
      }
    }
  ) { padding ->
    Box(modifier = Modifier
      .padding(padding)
      .fillMaxSize()) {
      when (tabIndex) {
        0 -> TrustedServerList(
          servers = trustedServers,
          remove = { trustedDevicesViewModel.removeTrustedServer(it) },
        )

        1 -> TrustedClientList(
          clients = trustedClients,
          remove = { trustedDevicesViewModel.removeTrustedClient(it) }
        )
      }
    }
  }
}
