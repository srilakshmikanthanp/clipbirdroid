package com.srilakshmikanthanp.clipbirdroid.ui.gui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.srilakshmikanthanp.clipbirdroid.clipboard.Clipboard

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
