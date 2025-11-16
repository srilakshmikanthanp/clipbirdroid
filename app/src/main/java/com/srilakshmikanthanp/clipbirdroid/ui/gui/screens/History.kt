package com.srilakshmikanthanp.clipbirdroid.ui.gui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.clipboard.ClipboardViewModel
import com.srilakshmikanthanp.clipbirdroid.history.HistoryViewModel
import com.srilakshmikanthanp.clipbirdroid.syncing.SyncingViewModel
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.ClipboardContentItem
import com.srilakshmikanthanp.clipbirdroid.ui.gui.composables.ClipboardContentSend

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun History(
  historyViewModel: HistoryViewModel = hiltViewModel<HistoryViewModel>(),
  syncingViewModel: SyncingViewModel = hiltViewModel<SyncingViewModel>(),
  clipboardViewModel: ClipboardViewModel = hiltViewModel<ClipboardViewModel>(),
  onMenuClick: () -> Unit = {},
) {
  val history by historyViewModel.clipboardHistory.history.collectAsState()

  val sendHandler: () -> Unit = {
    syncingViewModel.synchronize(clipboardViewModel.clipboardManager.getClipboard().getClipboardContent())
  }

  val onCopy = { idx: Int ->
    clipboardViewModel.clipboardManager.getClipboard().setClipboardContent(history[idx])
  }

  val onDelete = { idx: Int ->
    historyViewModel.clipboardHistory.deleteHistoryAt(idx)
  }

  val menuIcon = @Composable {
    IconButton(onClick = onMenuClick) {
      Image(
        painter = painterResource(R.drawable.menu),
        contentDescription = stringResource(id = R.string.menu)
      )
    }
  }

  val historyTopBar = @Composable {
    TopAppBar(
      navigationIcon = { menuIcon() },
      title = {
        Text(
          stringResource(id = R.string.clipbird_history),
          modifier = Modifier.padding(horizontal = 3.dp)
        )
      },
      modifier = Modifier.padding(3.dp),
    )
  }

  val historyContent = @Composable { padding: PaddingValues ->
    Box(modifier = Modifier.padding(padding), contentAlignment = Alignment.Center) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxWidth()
          .padding(10.dp),
      ) {
        Spacer(modifier = Modifier.padding(5.dp))

        ClipboardContentSend(
          modifier = Modifier.fillMaxWidth(),
          onSend = sendHandler,
        )

        if (history.isEmpty()) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
              .fillMaxWidth()
              .fillMaxHeight(),
          ) {
            Image(
              contentDescription = stringResource(id = R.string.history_prompt),
              painter = painterResource(id = R.drawable.history),
              modifier = Modifier.fillMaxSize(0.3f)
            )
            Text(
              text = stringResource(id = R.string.history_prompt),
              textAlign = TextAlign.Center,
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.padding(10.dp)
            )
          }
        } else {
          Spacer(modifier = Modifier.padding(15.dp))

          ClipboardContentItem(
            clipHistory = history,
            onCopy = onCopy,
            onDelete = onDelete,
          )
        }
      }
    }
  }

  Scaffold(
    topBar = historyTopBar,
    content = historyContent,
  )
}
