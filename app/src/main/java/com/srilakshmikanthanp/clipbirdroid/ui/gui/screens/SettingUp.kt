package com.srilakshmikanthanp.clipbirdroid.ui.gui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.constant.appVersion

/**
 * Setting Up Screen
 */
@Composable
fun SettingUp() {
  Column(
    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Show the Clip Bird Logo
    Image(
      painter = painterResource(R.mipmap.ic_launcher_foreground),
      contentDescription = "Logo"
    )

    // Show the Version
    Text(
      text  = "Version ${appVersion()}",
      color = Color.Gray
    )
  }
}
