package com.srilakshmikanthanp.clipbirdroid.ui.gui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.constant.appVersion

/**
 * Setting Up Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingUp() {
  Scaffold {
    Column(
      modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(it),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Show the Clip Bird Logo
      Image(
        painter = painterResource(R.mipmap.ic_launcher_foreground),
        contentDescription = stringResource(id = R.string.logo_label)
      )

      // Show the Version
      Text(
        text =  stringResource(id = R.string.version_label)+" ${appVersion()}",
        color = Color.Gray
      )
    }
  }
}
