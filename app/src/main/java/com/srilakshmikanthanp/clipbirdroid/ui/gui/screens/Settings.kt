package com.srilakshmikanthanp.clipbirdroid.ui.gui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.srilakshmikanthanp.clipbirdroid.ApplicationStateViewModel
import com.srilakshmikanthanp.clipbirdroid.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  viewModel: ApplicationStateViewModel = hiltViewModel<ApplicationStateViewModel>(),
  onMenuClick: () -> Unit = {},
) {
  val isBluetoothEnabled by viewModel.applicationState.shouldUseBluetoothFlow.collectAsState()
  var permissionGranted by remember { mutableStateOf(isBluetoothEnabled) }

  val bluetoothPermissions = remember {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
    } else {
      arrayOf(Manifest.permission.BLUETOOTH_ADMIN)
    }
  }

  val context = LocalContext.current

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { results ->
    permissionGranted = results.values.all { it }
    if (!permissionGranted) Toast.makeText(context, R.string.bluetooth_permission_denied, Toast.LENGTH_LONG).show()
    viewModel.applicationState.setShouldUseBluetooth(permissionGranted)
  }

  val menuIcon = @Composable {
    IconButton(onClick = onMenuClick) {
      Image(
        painter = painterResource(R.drawable.menu),
        contentDescription = stringResource(id = R.string.menu)
      )
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = { menuIcon() },
        title = { Text(stringResource(id = R.string.settings)) },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface
        )
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp, vertical = 24.dp),
      verticalArrangement = Arrangement.Top
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(
          modifier = Modifier.weight(1f)
        ) {
          Text(
            text = stringResource(id = R.string.use_bluetooth),
            style = MaterialTheme.typography.titleMedium
          )
          Text(
            text = stringResource(id = R.string.bluetooth_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Switch(
          checked = isBluetoothEnabled,
          onCheckedChange = { checked ->
            if (checked) {
              permissionLauncher.launch(bluetoothPermissions)
            } else {
              viewModel.applicationState.setShouldUseBluetooth(false)
            }
          }
        )
      }

      if (!permissionGranted && isBluetoothEnabled) {
        Text(
          text = stringResource(id = R.string.bluetooth_permission_denied),
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.padding(top = 8.dp)
        )
      }
    }
  }
}
