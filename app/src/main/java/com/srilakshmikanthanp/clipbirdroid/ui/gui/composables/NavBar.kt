package com.srilakshmikanthanp.clipbirdroid.ui.gui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.srilakshmikanthanp.clipbirdroid.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavBar(
  title: @Composable () -> Unit,
  onMenuClick: () -> Unit,
  onQRCodeClick: () -> Unit,
  onJoinClick: () -> Unit,
  onResetClick: () -> Unit,
  onAboutClick: () -> Unit,
  onIssueClick: () -> Unit,
  modifier: Modifier = Modifier,

  ) {
  TopAppBar(
    navigationIcon = {
      Image(
        painter = painterResource(R.drawable.menu),
        contentDescription = null,
        modifier = Modifier.padding(end = 10.dp)
      )
    },
    title = title,
    modifier = Modifier.padding(horizontal = 10.dp),
    // Actions like About and Quit
    actions = {
      Image(painter = painterResource(R.drawable.more), contentDescription = null)
    })
}
