package com.srilakshmikanthanp.clipbirdroid.ui.gui.modals;

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.utility.functions.encode

/**
 * Group modal to show the group details
 */
@Composable
fun Group(
  onDismissRequest: () -> Unit,
  title: String,
  code: String,
  port: Int,
  modifier: Modifier = Modifier,
) {
  // generate the qrcode from the code
  val qrcode = encode(code, MaterialTheme.colorScheme.onSurface)?.asImageBitmap()

  // dialog
  Dialog(onDismissRequest = onDismissRequest) {
    Card {
      Column (horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        if (qrcode != null) Image(qrcode, stringResource(id = R.string.qr_label))
        else Image(painterResource(R.drawable.broken), stringResource(id = R.string.error_label))
        Text(text = "$port", style = MaterialTheme.typography.bodySmall)
      }
    }
  }
}

/**
 * Preview
 */
@Preview(showBackground = true)
@Composable
private fun GroupPreview() {
  Group(onDismissRequest = {}, title =  stringResource(id = R.string.group_label), code =  stringResource(id = R.string.code_label), port = 1234)
}
