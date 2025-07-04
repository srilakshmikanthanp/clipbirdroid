package com.srilakshmikanthanp.clipbirdroid.ui.gui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.controller.AppController
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.DrawerItems
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.NavDrawer
import com.srilakshmikanthanp.clipbirdroid.ui.gui.screens.AboutUs
import com.srilakshmikanthanp.clipbirdroid.ui.gui.screens.Devices
import com.srilakshmikanthanp.clipbirdroid.ui.gui.screens.History
import com.srilakshmikanthanp.clipbirdroid.service.ClipbirdService
import com.srilakshmikanthanp.clipbirdroid.ui.gui.theme.ClipbirdTheme
import com.srilakshmikanthanp.clipbirdroid.utilities.functions.generateX509Certificate
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.srilakshmikanthanp.clipbirdroid.Clipbird


/**
 * Clipbird Composable
 */
@Composable
private fun Clipbird(controller: AppController) {
  // Composable States and Scope
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  var selected by remember { mutableStateOf(DrawerItems.DEVICES) }

  // Handler For Item Click
  val onItemClicked: (DrawerItems) -> Unit = {
    // Close the Drawer on Item Click
    scope.launch { drawerState.close() }

    // Handle the Item Click
    selected = it
  }

  // Menu click handler
  val onMenuClick: () -> Unit = {
    scope.launch {
      drawerState.open()
    }
  }

  // Render the Content
  NavDrawer(
    onItemClicked = onItemClicked,
    selected = selected,
    drawerState = drawerState,
  ) {
    when (selected) {
      DrawerItems.HISTORY -> History(controller, onMenuClick)
      DrawerItems.ABOUT -> AboutUs(onMenuClick)
      DrawerItems.DEVICES -> Devices(controller, onMenuClick)
    }
  }
}

/**
 * Preview Clipbird
 */
@Preview(showBackground = true)
@Composable
private fun PreviewClipbird() {
  Clipbird(AppController(generateX509Certificate(LocalContext.current), LocalContext.current))
}

/**
 * Main Activity
 */
class MainActivity : ComponentActivity() {
  // companion object
  companion object {
    const val QUIT_ACTION = "com.srilakshmikanthanp.clipbirdroid.ui.gui.SplashActivity.QUIT_ACTION"
  }

  @SuppressLint("BatteryLife")
  @OptIn(ExperimentalPermissionsApi::class)
  @Composable
  private fun RequestPermissionsAndStartService(p: MutableList<String>) {
    // Should show Alert Dialog
    var isShowingAlert by remember { mutableStateOf(false) }

    // Multiple permissions state hook
    val permissions = rememberMultiplePermissionsState(p)

    // finalize
    val finalize : () -> Unit = {
      val powerManager: PowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
      val action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
      val uri = "package:${this.packageName}".toUri()

      // if already ignored
      if (!powerManager.isIgnoringBatteryOptimizations(this.packageName)) {
        Intent(action, uri).also { startActivity(it) }
      }

      // start main activity
      ClipbirdService.start(this)
    }

    // onConfirm
    val onConfirm : () -> Unit = {
      isShowingAlert = false; permissions.launchMultiplePermissionRequest()
    }

    // on Dismiss
    val onDismiss : () -> Unit = {
      isShowingAlert = false; finalize()
    }

    // request permissions
    LaunchedEffect(permissions) {
      permissions.launchMultiplePermissionRequest()
    }

    // if all permissions granted or not show rationale
    if (permissions.allPermissionsGranted) {
      finalize().also { return }
    }

    // is need to show rationale
    if (permissions.shouldShowRationale) {
      isShowingAlert = true
    }

    // show dialog for permissions
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

  // handle Intent
  private fun handleIntent(intent: Intent?) {
    if (intent?.action == QUIT_ACTION) {
      ClipbirdService.stop(this).also { this.finishAndRemoveTask() }
    }
  }

  // On Create
  override fun onCreate(savedInstanceState: Bundle?) {
    // create splash screen instance
    val splashScreen = installSplashScreen()

    // set exit animation
    splashScreen.setOnExitAnimationListener {
      it.view.animate().withEndAction { it.remove() }.start()
    }

    // call super class method
    super.onCreate(savedInstanceState)

    // Initialize App Controller
    (application as Clipbird).initialize()

    // Permissions defined on Manifest
    val permissions = mutableListOf<String>()

    // if api >= 34
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      permissions.add(Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC)
    }

    // if api >= 33
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    // Add Receive boot completed
    permissions.add(Manifest.permission.RECEIVE_BOOT_COMPLETED)

    // check self permissions
    permissions.removeIf {
      checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
    }

    // get controller
    val controller = (application as Clipbird).getController()

    // Set Content
    setContent {
      ClipbirdTheme {
        Clipbird(controller).also { RequestPermissionsAndStartService(permissions) }
      }
    }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent).also { handleIntent(intent) }
  }

  // on Start
  override fun onStart() {
    super.onStart().also { handleIntent(intent) }
  }
}
