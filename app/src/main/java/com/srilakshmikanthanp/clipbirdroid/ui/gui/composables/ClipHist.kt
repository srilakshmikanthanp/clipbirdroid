package com.srilakshmikanthanp.clipbirdroid.ui.gui.composables

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.clipboard.Clipboard

/**
 * Convert the ByteArray to Bitmap
 */
private fun convertPngToBitmap(content: ByteArray): ImageBitmap {
  return BitmapFactory.decodeByteArray(content, 0, content.size).asImageBitmap()
}

/**
 * Convert the ByteArray to String
 */
private fun convertTextToString(content: ByteArray): String {
  return String(content)
}

/**
 * Clipboard data mime type, content
 */
typealias ClipData = List<Pair<String, ByteArray>>

/**
 * Is the ClipData can be inferred as Image
 */
private fun ClipData.isImage(): ImageBitmap? {
  val content = firstOrNull { it.first == Clipboard.MIME_TYPE_PNG }?.second ?: return null
  return convertPngToBitmap(content)
}

/**
 * Is the ClipData can be inferred as Text
 */
private fun ClipData.isText(): String? {
  val content = firstOrNull { it.first == Clipboard.MIME_TYPE_TEXT }?.second ?: return null
  return convertTextToString(content)
}

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
  Text(text = text)
}

/**
 * ClipData to Tile
 */
@Composable
private fun ClipData.ToTile() {
  isImage()?.let { ImageTile(it) } ?: isText()?.let { TextTile(it) } ?: throw Exception("Unknown")
}

/**
 * Copy Action
 */
@Composable
private fun CopyAction(onClick: () -> Unit = {}) {
  Image(painterResource(R.drawable.copy), "copy", Modifier.clickable { onClick })
}

/**
 * Delete Action
 */
@Composable
private fun DeleteAction(onClick: () -> Unit = {}) {
  Image(painterResource(R.drawable.delete), "delete", Modifier.clickable { onClick })
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
  ElevatedCard(modifier = modifier) {
    Column {
      // Modifier for Row that presents content from start and end
      val rowModifierStart = Modifier
        .padding(horizontal = 10.dp, vertical = 10.dp)
        .fillMaxWidth()

      // Row that presents content from start
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = rowModifierStart,
        horizontalArrangement = Arrangement.Start,
      ) {
        // Show the Clipboard Content
        content.ToTile()
      }

      // Modifier for Row that presents content from end
      val rowModifierEnd = Modifier
        .padding(horizontal = 10.dp, vertical = 10.dp)
        .fillMaxWidth()

      // Row that presents content from end
      Row(
        horizontalArrangement = Arrangement.spacedBy(30.dp, Alignment.End),
        modifier = rowModifierEnd,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Show the Copy Action Button
        CopyAction(onCopy)

        // Show the Delete Action
        DeleteAction(onDelete)
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
fun ClipHist(
  clipHistory: List<ClipData>,
  modifier: Modifier = Modifier,
  onCopy: (Int) -> Unit,
  onDelete: (Int) -> Unit
) {
  // Item Modifier for the ClipTile Composable
  val itemModifier = Modifier.fillMaxWidth().padding(2.dp)

  // Lazy Column for the Clip History
  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(4.dp),
    modifier = modifier,
    contentPadding = PaddingValues(5.dp)
  ) {
    items(clipHistory.size) {
      ClipTile(clipHistory[it], itemModifier, { onCopy(it) }, { onDelete(it) })
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
  ClipHist(
    clipHistory = clipHistory,
    modifier = Modifier.fillMaxWidth(),
    onCopy = {},
    onDelete = {},
  )
}
