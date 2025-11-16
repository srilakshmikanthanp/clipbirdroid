package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.clipboard.ClipboardManager
import com.srilakshmikanthanp.clipbirdroid.syncing.manager.SyncingManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SendHandler : ComponentActivity() {
  @Inject lateinit var clipboardManager: ClipboardManager
  @Inject lateinit var syncingManager: SyncingManager

  @OptIn(DelicateCoroutinesApi::class)
  override fun onWindowFocusChanged(hasFocus: Boolean) {
    if (!hasFocus) return

    val content = clipboardManager.getClipboard().getClipboardContent()
    lifecycleScope.launch { syncingManager.synchronize(content) }

    runOnUiThread {
      Toast.makeText(this, R.string.synced, Toast.LENGTH_SHORT).show()
      this.finish()
    }
  }

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onStart() {
    super.onStart()

    setContent {
      Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator()
        }
      }
    }
  }
}
