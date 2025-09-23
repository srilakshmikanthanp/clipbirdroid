package com.srilakshmikanthanp.clipbirdroid.ui.gui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.srilakshmikanthanp.clipbirdroid.clipboard.Clipboard

@Composable
fun ClipHistory(
  clipHistory: List<ClipData>,
  modifier: Modifier = Modifier,
  onCopy: (Int) -> Unit,
  onDelete: (Int) -> Unit
) {
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

@Preview(showBackground = true)
@Composable
private fun ClipHistoryPreview() {
  val clipHistory = listOf(
    listOf(Clipboard.MIME_TYPE_TEXT to "This is Text Item".toByteArray()),
    listOf(Clipboard.MIME_TYPE_TEXT to "This is Text Item".toByteArray()),
  )

  ClipHistory(
    clipHistory = clipHistory,
    modifier = Modifier.fillMaxWidth(),
    onCopy = {},
    onDelete = {},
  )
}
