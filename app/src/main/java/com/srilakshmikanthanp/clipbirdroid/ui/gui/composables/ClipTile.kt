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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.clipboard.Clipboard

typealias ClipData = List<Pair<String, ByteArray>>

private fun ByteArray.toBitmap(): ImageBitmap {
  return BitmapFactory.decodeByteArray(this, 0, this.size).asImageBitmap()
}

@Composable
private fun NoPreview(image: Int) {
  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
    Image(painterResource(image), stringResource(id = R.string.clipbird_content))
    Text(text = stringResource(id = R.string.no_preview))
  }
}

@Composable
fun ClipTile(
  content: ClipData,
  modifier: Modifier = Modifier,
  onCopy: () -> Unit = {},
  onDelete: () -> Unit = {}
) {
  Card(modifier = modifier) {
    Column(modifier = Modifier.padding(5.dp)) {
      val rowModifierStart = Modifier
        .padding(horizontal = 10.dp, vertical = 15.dp)
        .fillMaxWidth()
        .fillMaxHeight(0.65f)

      val maxImgSize = 3145728
      val maxTxtSize = 200

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

      val rowModifierEnd = Modifier.padding(vertical = 6.dp).fillMaxWidth()

      Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.End),
        modifier = rowModifierEnd,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = onCopy) {
          Image(
            painterResource(R.drawable.copy),
            stringResource(id = R.string.copy)
          )
        }

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

@Preview(showBackground = true)
@Composable
private fun ClipTilePreview() {
  val content = listOf(Clipboard.MIME_TYPE_TEXT to "Hello World".toByteArray())

  ClipTile(
    modifier = Modifier.fillMaxWidth(),
    content = content
  )
}
