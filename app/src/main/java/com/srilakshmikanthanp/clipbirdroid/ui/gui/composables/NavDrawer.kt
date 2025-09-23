package com.srilakshmikanthanp.clipbirdroid.ui.gui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
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
import com.srilakshmikanthanp.clipbirdroid.constants.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.constants.appName

enum class DrawerItems {
  DEVICES, HISTORY, ACCOUNT, ABOUT
}

@Composable
private fun DrawerContent(
  onItemClicked: (DrawerItems) -> Unit,
  selected: DrawerItems,
) {
  ModalDrawerSheet (
    modifier = Modifier.fillMaxWidth(fraction = 0.8f),
  ) {
    Column (modifier = Modifier.padding(10.dp)) {
      Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(10.dp).fillMaxWidth()
      ) {
        Image(painter = painterResource(R.drawable.phone), contentDescription = stringResource(id = R.string.phone))
        Text(style = MaterialTheme.typography.headlineMedium, text = appName())
        Text(text = appMdnsServiceName(LocalContext.current), color = Color.Gray)
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

      NavigationDrawerItem(
        onClick =  { onItemClicked(DrawerItems.DEVICES) },
        label = { Text(text =  stringResource(id = R.string.devices)) },
        selected = selected == DrawerItems.DEVICES,
      )

      NavigationDrawerItem(
        onClick =  { onItemClicked(DrawerItems.HISTORY) },
        label = { Text(text = stringResource(id = R.string.history)) },
        selected = selected == DrawerItems.HISTORY,
      )

      NavigationDrawerItem(
        onClick =  { onItemClicked(DrawerItems.ACCOUNT) },
        label = { Text(text = stringResource(id = R.string.account)) },
        selected = selected == DrawerItems.ACCOUNT,
      )

      HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

      NavigationDrawerItem(
        onClick =  { onItemClicked(DrawerItems.ABOUT) },
        label = { Text(text = stringResource(id = R.string.about)) },
        selected = selected == DrawerItems.ABOUT,
      )
    }
    }
}

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

@Preview(showBackground = true)
@Composable
private fun NavDrawerPreview() {
  NavDrawer(
    drawerState = DrawerState(DrawerValue.Open),
    onItemClicked = {},
    selected = DrawerItems.DEVICES
  )
}
