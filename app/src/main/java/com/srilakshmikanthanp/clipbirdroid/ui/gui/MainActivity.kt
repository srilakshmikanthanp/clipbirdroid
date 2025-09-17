package com.srilakshmikanthanp.clipbirdroid.ui.gui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.service.ClipbirdService
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.DrawerItems
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.NavDrawer
import com.srilakshmikanthanp.clipbirdroid.ui.gui.screens.AboutUs
import com.srilakshmikanthanp.clipbirdroid.ui.gui.screens.Devices
import com.srilakshmikanthanp.clipbirdroid.ui.gui.screens.History
import com.srilakshmikanthanp.clipbirdroid.ui.gui.theme.ClipbirdTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@Composable
private fun Clipbird() {
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  var selected by remember { mutableStateOf(DrawerItems.DEVICES) }

  val onItemClicked: (DrawerItems) -> Unit = {
    scope.launch {
      drawerState.close()
    }
    selected = it
  }

  val onMenuClick: () -> Unit = {
    scope.launch {
      drawerState.open()
    }
  }

  NavDrawer(
    onItemClicked = onItemClicked,
    selected = selected,
    drawerState = drawerState,
  ) {
    when (selected) {
      DrawerItems.HISTORY -> History(onMenuClick = onMenuClick)
      DrawerItems.ABOUT -> AboutUs(onMenuClick)
      DrawerItems.DEVICES -> Devices(onMenuClick = onMenuClick)
      DrawerItems.ACCOUNT -> {}
    }
  }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  private val connection = object : ServiceConnection {
    override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
      val clipbirdBinder = binder as? ClipbirdService.ClipbirdBinder ?: return
      this@MainActivity.clipbirdBinder = clipbirdBinder
      val permissions = mutableListOf<String>()

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        permissions.add(Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC)
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
      }

      permissions.add(Manifest.permission.RECEIVE_BOOT_COMPLETED)

      permissions.removeIf {
        checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
      }

      setContent {
        ClipbirdTheme {
          Clipbird().also { RequestPermissionsAndStartService(permissions) }
        }
      }
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
      clipbirdBinder = null
    }
  }

  private var clipbirdBinder: ClipbirdService.ClipbirdBinder? = null

  companion object {
    const val QUIT_ACTION = "com.srilakshmikanthanp.clipbirdroid.ui.gui.SplashActivity.QUIT_ACTION"
  }

  @SuppressLint("BatteryLife")
  @OptIn(ExperimentalPermissionsApi::class)
  @Composable
  private fun RequestPermissionsAndStartService(p: MutableList<String>) {
    var isShowingAlert by remember { mutableStateOf(false) }
    val permissions = rememberMultiplePermissionsState(p)

    val finalize : () -> Unit = {
      val powerManager: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
      val action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
      val uri = "package:${this.packageName}".toUri()

      if (!powerManager.isIgnoringBatteryOptimizations(this.packageName)) {
        Intent(action, uri).also { startActivity(it) }
      }

      ClipbirdService.start(this)
    }

    val onConfirm : () -> Unit = {
      isShowingAlert = false; permissions.launchMultiplePermissionRequest()
    }

    val onDismiss : () -> Unit = {
      isShowingAlert = false; finalize()
    }

    LaunchedEffect(permissions) {
      permissions.launchMultiplePermissionRequest()
    }

    if (permissions.allPermissionsGranted) {
      finalize().also { return }
    }

    if (permissions.shouldShowRationale) {
      isShowingAlert = true
    }

    if(isShowingAlert) AlertDialog(
      onDismissRequest = { onDismiss() },
      title = { Text(getString(R.string.permissions)) },
      text = { Text(getString(R.string.required_permissions)) },
      confirmButton = {
        TextButton(
          onClick = { onConfirm() }
        ) {
          Text(getString(R.string.allow))
        }},
      dismissButton = {
        TextButton(
          onClick = { onDismiss() }
        ) {
          Text(getString(R.string.deny))
        }},
    )
  }

  private fun handleIntent(intent: Intent?) {
    if (intent?.action == QUIT_ACTION) {
      ClipbirdService.stop(this).also { this.finishAndRemoveTask() }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    val splashScreen = installSplashScreen()
    splashScreen.setOnExitAnimationListener {
      it.view.animate().withEndAction { it.remove() }.start()
    }
    super.onCreate(savedInstanceState)
    Intent(this, ClipbirdService::class.java).also { intent ->
      bindService(intent, connection, BIND_AUTO_CREATE)
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent).also { handleIntent(intent) }
  }

  override fun onStart() {
    super.onStart().also { handleIntent(intent) }
  }

  override fun onDestroy() {
    super.onDestroy()
    if (clipbirdBinder != null) {
      unbindService(connection)
      clipbirdBinder = null
    }
  }
}
