package com.srilakshmikanthanp.clipbirdroid.ui.gui.composables

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.clipboard.Clipboard

/**
 * Convert the ByteArray to Bitmap
 */
private fun ByteArray.toBitmap(): ImageBitmap {
  return BitmapFactory.decodeByteArray(this, 0, this.size).asImageBitmap()
}

/**
 * Clipboard data mime type, content
 */
typealias ClipData = List<Pair<String, ByteArray>>

/**
 * Image Tile
 */
@Composable
private fun ImageTile(image: ImageBitmap) {
  Image(image, "Clipboard Content")
}

/**
 * Text Tile
 */
@Composable
private fun TextTile(text: String) {
  Text(text = text, overflow = TextOverflow.Ellipsis, maxLines = 4)
}

/**
 * ClipTile Composable
 */
@Composable
private fun ClipTile(
  content: ClipData,
  modifier: Modifier = Modifier,
  onCopy: () -> Unit = {},
  onDelete: () -> Unit = {}
) {
  Card(modifier = modifier) {
    Column (modifier = Modifier.padding(5.dp)) {
      // Modifier for Row that presents content from start and end
      val rowModifierStart = Modifier.padding(horizontal = 10.dp, vertical = 15.dp).fillMaxWidth()
        .fillMaxHeight(0.65f)

      // Row that presents content from start
      Row(
        horizontalArrangement = Arrangement.Start,
        modifier = rowModifierStart,
      ) {
        for (clip in content) {
          if (clip.first == Clipboard.MIME_TYPE_TEXT) {
            TextTile(text = String(clip.second))
            break
          }

          if (clip.first == Clipboard.MIME_TYPE_PNG) {
            ImageTile(clip.second.toBitmap())
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
          Image(painterResource(R.drawable.copy), "copy")
        }

        // Show the Delete Action
        IconButton(onClick = onDelete) {
          Image(painterResource(R.drawable.delete), "delete")
        }
      }
    }
  }
}

/**
 * Preview of ClipTile
 */
@Preview(showBackground = true)
@Composable
private fun ClipTilePreview() {
  // Clipboard Content For Demo
  val content = listOf(Clipboard.MIME_TYPE_TEXT to "Hello World".toByteArray())

  // Clip Tile
  ClipTile(
    modifier = Modifier.fillMaxWidth(),
    content = content
  )
}

/**
 * Clip History Composable
 */
@Composable
fun ClipHistory(
  clipHistory: List<ClipData>,
  modifier: Modifier = Modifier,
  onCopy: (Int) -> Unit,
  onDelete: (Int) -> Unit
) {
  // Lazy Column for the Clip History
  LazyColumn(
    contentPadding = PaddingValues(top = 5.dp, bottom = 5.dp),
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    items(clipHistory.size) {
      ClipTile(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        content = clipHistory[it],
        onCopy = { onCopy(it) },
        onDelete = { onDelete(it) }
      )
    }
  }
}

/**
 * Preview for the ClipHist Composable
 */
@Preview(showBackground = true)
@Composable
private fun ClipHistPreview() {
  // Clipboard Content For Demo
  val clipHistory = listOf(
    listOf(Clipboard.MIME_TYPE_TEXT to "This is Text Item".toByteArray()),
    listOf(Clipboard.MIME_TYPE_TEXT to "This is Text Item".toByteArray()),
  )

  // Clip History
  ClipHistory(
    clipHistory = clipHistory,
    modifier = Modifier.fillMaxWidth(),
    onCopy = {},
    onDelete = {},
  )
}
