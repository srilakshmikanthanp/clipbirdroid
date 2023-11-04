package com.srilakshmikanthanp.clipbirdroid.ui.gui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.srilakshmikanthanp.clipbirdroid.R

/**
 * Clipbird Bar that act as a TopAppBar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipbirdBar(
  title: @Composable () -> Unit,
  onMenuClick: () -> Unit,
  actions: @Composable RowScope.() -> Unit,
  modifier: Modifier = Modifier
) {
  // Navigation Icon is the Menu Icon
  val menuIcon = @Composable {
    IconButton(onClick = onMenuClick) {
      Image(painter = painterResource(R.drawable.menu), contentDescription = "Menu",)
    }
  }

  // Show the Top App Bar
  TopAppBar(
    navigationIcon = { menuIcon() },
    title = title,
    modifier = modifier,
    actions = actions,
  )
}
