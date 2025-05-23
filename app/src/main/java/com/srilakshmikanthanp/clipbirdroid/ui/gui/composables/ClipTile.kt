package com.srilakshmikanthanp.clipbirdroid.ui.gui.composables

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.clipboard.Clipboard

/**
 * Clipboard data mime type, content
 */
typealias ClipData = List<Pair<String, ByteArray>>

/**
 * Convert the ByteArray to Bitmap
 */
private fun ByteArray.toBitmap(): ImageBitmap {
  return BitmapFactory.decodeByteArray(this, 0, this.size).asImageBitmap()
}

/**
 * Image Tile No preview
 */
@Composable
private fun NoPreview(image: Int) {
  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
    Image(painterResource(image), stringResource(id = R.string.clipbird_content))
    Text(text = stringResource(id = R.string.no_preview))
  }
}

/**
 * ClipTile Composable
 */
@Composable
fun ClipTile(
  content: ClipData,
  modifier: Modifier = Modifier,
  onCopy: () -> Unit = {},
  onDelete: () -> Unit = {}
) {
  Card(modifier = modifier) {
    Column(modifier = Modifier.padding(5.dp)) {
      // Modifier for Row that presents content from start and end
      val rowModifierStart = Modifier
        .padding(horizontal = 10.dp, vertical = 15.dp)
        .fillMaxWidth()
        .fillMaxHeight(0.65f)

      // max image size
      val maxImgSize = 3145728; // 3 MB
      val maxTxtSize = 200;

      // Row that presents content from start
      Row(
        horizontalArrangement = Arrangement.Start,
        modifier = rowModifierStart,
      ) {
        for (clip in content) {
          if (clip.first == Clipboard.MIME_TYPE_PNG && clip.second.size < maxImgSize) {
            Image(clip.second.toBitmap(), stringResource(id = R.string.clipbird_content))
            break
          } else if (clip.first == Clipboard.MIME_TYPE_PNG) {
            NoPreview(R.drawable.photo)
            break
          }

          if (clip.first == Clipboard.MIME_TYPE_TEXT) {
            val text = String(clip.second).take(maxTxtSize)
            Text(text = text, overflow = TextOverflow.Ellipsis, maxLines = 4)
            break
          }
        }
      }

      // Modifier for Row that presents content from end
      val rowModifierEnd = Modifier.padding(vertical = 6.dp).fillMaxWidth()

      // Row that presents content from end
      Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.End),
        modifier = rowModifierEnd,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Show the Copy Action Button
        IconButton(onClick = onCopy) {
          Image(
            painterResource(R.drawable.copy),
            stringResource(id = R.string.copy)
          )
        }

        // Show the Delete Action
        IconButton(onClick = onDelete) {
          Image(
            painterResource(R.drawable.delete),
            stringResource(id = R.string.delete)
          )
        }
      }
    }
  }
}
