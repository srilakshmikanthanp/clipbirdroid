package com.srilakshmikanthanp.clipbirdroid.ui.gui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.constant.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.constant.appName

/**
 * Enum Class for Drawer Items
 */
enum class DrawerItems {
  DEVICES, HISTORY, ABOUT
}

/**
 * Drawer Content Composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerContent(
  onItemClicked: (DrawerItems) -> Unit,
  selected: DrawerItems,
) {
  ModalDrawerSheet (
    windowInsets = WindowInsets(10.dp, 10.dp, 10.dp, 10.dp),
    modifier = Modifier.requiredWidth(280.dp),
  ) {
    // Header for the Drawer
    Column (
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .padding(15.dp)
        .fillMaxWidth()
    ) {
      Image(painter = painterResource(R.drawable.phone), contentDescription = stringResource(id = R.string.phone_label))
      Text(style = MaterialTheme.typography.headlineMedium, text = appName())
      Text(text = appMdnsServiceName(LocalContext.current), color = Color.Gray)
    }

    // Divider for Drawer
    Divider(modifier = Modifier.padding(vertical = 15.dp))

    // Item For Device
    NavigationDrawerItem(
      onClick =  { onItemClicked(DrawerItems.DEVICES) },
      label = { Text(text =  stringResource(id = R.string.devices_label)) },
      selected = selected == DrawerItems.DEVICES,
    )

    // Item For History
    NavigationDrawerItem(
      onClick =  { onItemClicked(DrawerItems.HISTORY) },
      label = { Text(text = stringResource(id = R.string.history_label2)) },
      selected = selected == DrawerItems.HISTORY,
    )

    // Divider for Drawer
    Divider(modifier = Modifier.padding(vertical = 15.dp))

    // Item For About
    NavigationDrawerItem(
      onClick =  { onItemClicked(DrawerItems.ABOUT) },
      label = { Text(text = stringResource(id = R.string.about_label)) },
      selected = selected == DrawerItems.ABOUT,
    )
  }
}

/**
 * Navigation Drawer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavDrawer(
  onItemClicked: (DrawerItems) -> Unit,
  selected: DrawerItems,
  drawerState: DrawerState,
  content: @Composable () -> Unit = {}
) {
  ModalNavigationDrawer(
    drawerContent = { DrawerContent(onItemClicked, selected) },
    drawerState = drawerState
  ) {
    content()
  }
}

/**
 * Preview
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun NavDrawerPreview() {
  NavDrawer(
    drawerState = DrawerState(DrawerValue.Open),
    onItemClicked = {},
    selected = DrawerItems.DEVICES
  )
}
