package com.srilakshmikanthanp.clipbirdroid.ui.gui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.srilakshmikanthanp.clipbirdroid.controller.AppController
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.DrawerItems
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.NavDrawer
import com.srilakshmikanthanp.clipbirdroid.ui.gui.screens.AboutUs
import com.srilakshmikanthanp.clipbirdroid.ui.gui.screens.Devices
import com.srilakshmikanthanp.clipbirdroid.ui.gui.screens.History
import com.srilakshmikanthanp.clipbirdroid.ui.gui.service.ClipbirdService
import com.srilakshmikanthanp.clipbirdroid.ui.gui.theme.ClipbirdTheme
import com.srilakshmikanthanp.clipbirdroid.utility.functions.generateX509Certificate
import kotlinx.coroutines.launch


/**
 * Clipbird Composable
 */
@OptIn(ExperimentalMaterial3Api::class)
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

  // handle Intent
  private fun handleIntent(intent: Intent?) {
    if (intent?.action == QUIT_ACTION) {
      ClipbirdService.stop(this).also { this.finishAndRemoveTask() }
    }
  }

  // On Create
  override fun onCreate(savedInstanceState: Bundle?) {
    // Initialize the Controller & Set Content
    super.onCreate(savedInstanceState)
    val controller = (this.application as Clipbird).getController()
    setContent { ClipbirdTheme { Clipbird(controller) } }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent).also { handleIntent(intent) }
  }

  // on Start
  override fun onStart() {
    super.onStart().also { handleIntent(intent) }
  }
}
