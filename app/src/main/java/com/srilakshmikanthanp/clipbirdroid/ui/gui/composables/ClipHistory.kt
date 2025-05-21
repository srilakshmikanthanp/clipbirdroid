package com.srilakshmikanthanp.clipbirdroid.ui.gui.composables

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.clipboard.Clipboard


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
private fun ClipHistoryPreview() {
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
