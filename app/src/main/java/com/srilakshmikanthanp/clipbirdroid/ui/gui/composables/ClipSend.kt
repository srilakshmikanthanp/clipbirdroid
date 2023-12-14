package com.srilakshmikanthanp.clipbirdroid.ui.gui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srilakshmikanthanp.clipbirdroid.R

/**
 * ClipSend Composable
 */
@Composable
fun ClipSend(modifier: Modifier = Modifier, onSend: () -> Unit = {}) {
  // Row for the Text and send Button
  Card (modifier = modifier) {
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth().padding(10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        modifier = Modifier.padding(horizontal = 5.dp).weight(1.5f, false),
        text = stringResource(id = R.string.send_content),
        fontSize = 14.sp,
      )

      Button(
        modifier = Modifier.weight(0.5f, true),
        onClick = onSend
      ) {
        Text(text = stringResource(id = R.string.send))
      }
    }
  }
}

/**
 * Preview of ClipSend
 */
@Preview(showBackground = true)
@Composable
fun ClipSendPreview() {
  ClipSend(
    modifier = Modifier.fillMaxWidth().padding(10.dp),
  )
}
