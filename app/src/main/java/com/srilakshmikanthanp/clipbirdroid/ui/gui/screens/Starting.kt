package com.srilakshmikanthanp.clipbirdroid.ui.gui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.srilakshmikanthanp.clipbirdroid.R

/**
 * Setting Up Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Starting() {
  Scaffold {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(it),
      verticalArrangement = Arrangement.Center,
    ) {
      Image(
        painter = painterResource(R.mipmap.ic_launcher_foreground),
        contentDescription = stringResource(id = R.string.logo)
      )
    }
  }
}
