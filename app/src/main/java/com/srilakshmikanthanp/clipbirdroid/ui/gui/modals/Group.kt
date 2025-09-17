package com.srilakshmikanthanp.clipbirdroid.ui.gui.modals;

import android.graphics.Bitmap
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.srilakshmikanthanp.clipbirdroid.R
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

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
  // Function to generate QR code from string using zxing library
  fun encode(value: String, color: Color, size: Size = Size(800, 800)): Bitmap? {
    val matrix: BitMatrix = try {
      MultiFormatWriter().encode(value, BarcodeFormat.QR_CODE, size.width, size.height)
    } catch (e: Exception) {
      return null
    }

    val bmp = createBitmap(matrix.width, matrix.height)

    for (x in 0 until matrix.width) {
      for (y in 0 until matrix.height) {
        bmp[x, y] = if (matrix[x, y]) color.toArgb() else Color.Transparent.toArgb()
      }
    }

    return bmp
  }

  // generate the qrcode from the code
  val qrcode = encode(code, MaterialTheme.colorScheme.onSurface)?.asImageBitmap()

  // dialog
  Dialog(onDismissRequest = onDismissRequest) {
    Card {
      Column (horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)

        if (qrcode != null) {
          Image(
            contentDescription = stringResource(id = R.string.qrcode),
            bitmap = qrcode,
            modifier = Modifier.padding(10.dp),
          )
        } else {
          Image(
            painterResource(R.drawable.broken),
            stringResource(id = R.string.error),
            modifier = Modifier.padding(10.dp)
          )
        }

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
  Group(onDismissRequest = {}, title =  stringResource(id = R.string.group), code =  stringResource(id = R.string.code), port = 1234)
}
